package com.getjobs.worker.liepin;

import com.getjobs.worker.utils.PlaywrightUtil;
import com.getjobs.application.service.LiepinService;
import com.getjobs.application.entity.LiepinEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

// Remove unused imports
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static com.getjobs.worker.liepin.Locators.*;


/**
 * @author loks666
 * Project: <a href="https://github.com/loks666/get_jobs">https://github.com/loks666/get_jobs</a>
 */
@Slf4j
@Component
@Scope("prototype")
public class Liepin {
    static {
        // Set log file name at class load time
        System.setProperty("log.name", "liepin");
    }

    private int maxPage = 50;
    private final List<String> resultList = new ArrayList<>();
    private final List<LiepinEntity> lastApiEntities = new ArrayList<>();
    private boolean monitoringRegistered = false;
    @Setter
    private LiepinConfig config;
    @Getter
    private Date startDate;
    @Setter
    private Page page;
    @Autowired
    private LiepinService liepinService;

    public interface ProgressCallback {
        void onProgress(String message, Integer current, Integer total);
    }

    @Setter
    private ProgressCallback progressCallback;
    @Setter
    private Supplier<Boolean> shouldStopCallback;

    public void prepare() {
        this.startDate = new Date();
        this.resultList.clear();

        // Monitor Liepin API requests and responses
        if (page != null && !monitoringRegistered) {
            // Request interception (search job API only)
            page.onRequest(req -> {
                try {
                    String url = req.url();
                    if (url != null && url.contains("com.liepin.searchfront4c.pc-search-job")
                            && !url.contains("com.liepin.searchfront4c.pc-search-job-cond-init")) {
                        // log.info("[Intercept][Request] {}", url);
                    }
                } catch (Exception ignored) {}
            });
            page.onResponse((Response response) -> {
                try {
                    String url = response.url();
                    if (url != null && response.status() == 200 &&
                            url.contains("com.liepin.searchfront4c.pc-search-job") &&
                            !url.contains("com.liepin.searchfront4c.pc-search-job-cond-init")) {
                        log.info("[Intercept][Response] {}", url);
                        String contentType = null;
                        try { contentType = response.headers().get("content-type"); } catch (Exception ignored) {}
                        if (contentType == null || contentType.contains("application/json")) {
                            String text = response.text();
                            if (text != null && !text.isEmpty()) {
                                // Try to parse; return if structure doesn't match
                                parseAndPersistLiepinData(text);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error monitoring Liepin API response: {}", e.getMessage());
                }
            });
            monitoringRegistered = true;
        }
    }

    public int execute() {
        if (page == null) {
            throw new IllegalStateException("Liepin.page is not set");
        }
        if (config == null) {
            throw new IllegalStateException("Liepin.config is not set");
        }

        // Ensure API monitoring is registered before execution
        prepare();

        List<String> keywords = config.getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            log.warn("No keywords configured, execution ended");
            return 0;
        }

        for (String keyword : keywords) {
            if (shouldStop()) {
                info("Stop signal received, ending keyword loop early");
                break;
            }
            submit(keyword);
        }
        return resultList.size();
    }

    // ========== Parse API JSON and save to DB ==========
    private void parseAndPersistLiepinData(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            // Compatible with both structures: data.data.jobCardList or data.jobCardList
            JsonNode cardList = root.path("data").path("data").path("jobCardList");
            if (!cardList.isArray()) {
                cardList = root.path("data").path("jobCardList");
            }
            if (!cardList.isArray()) {
                return;
            }
            lastApiEntities.clear();
            for (JsonNode item : cardList) {
                JsonNode job = item.path("job");
                JsonNode comp = item.path("comp");
                JsonNode recruiter = item.path("recruiter");

                Long jobId = readLong(job.path("jobId"));
                if (jobId == null) {
                    continue;
                }

                LiepinEntity entity = new LiepinEntity();
                entity.setJobId(jobId);
                entity.setJobTitle(readText(job.path("title")));
                entity.setJobLink(readText(job.path("link")));
                entity.setJobSalaryText(readText(job.path("salary")));
                entity.setJobArea(readText(job.path("dq")));
                entity.setJobEduReq(readText(job.path("requireEduLevel")));
                entity.setJobExpReq(readText(job.path("requireWorkYears")));
                entity.setJobPublishTime(readText(job.path("refreshTime")));

                entity.setCompId(readLong(comp.path("compId")));
                entity.setCompName(readText(comp.path("compName")));
                entity.setCompIndustry(readText(comp.path("compIndustry")));
                entity.setCompScale(readText(comp.path("compScale")));

                entity.setHrId(readText(recruiter.path("recruiterId")));
                entity.setHrName(readText(recruiter.path("recruiterName")));
                entity.setHrTitle(readText(recruiter.path("recruiterTitle")));
                entity.setHrImId(readText(recruiter.path("imId")));

                // Cache in memory for delivery display (avoid reading text from page)
                lastApiEntities.add(entity);
            }
            // Batch persist: insert only if not exists, default delivered=0
            try {
                liepinService.insertSnapshotsIfNotExistsBatch(lastApiEntities);
            } catch (Exception e) {
                log.warn("Failed to batch save Liepin job data: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to parse Liepin JSON: {}", e.getMessage());
        }
    }

    private String readText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String v = node.asText();
        return (v == null || v.isEmpty()) ? null : v;
    }

    private Long readLong(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        try {
            if (node.isNumber()) {
                long v = node.asLong();
                return v == 0 ? null : v;
            }
            if (node.isTextual()) {
                String t = node.asText();
                if (t == null || t.isEmpty()) return null;
                long v = Long.parseLong(t.trim());
                return v == 0 ? null : v;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String safeText(String s) {
        if (s == null) return null;
        return s.replaceAll("\n", " ").replaceAll("%s ", "[").replaceAll(" ", "]");
    }

    private boolean shouldStop() {
        return shouldStopCallback != null && Boolean.TRUE.equals(shouldStopCallback.get());
    }

    private void info(String msg) {
        if (progressCallback != null) {
            progressCallback.onProgress(msg, null, null);
        } else {
            log.info(msg);
        }
    }

    private void submit(String keyword) {
        // Clean keyword: remove quotes and extra whitespace
        String cleanKeyword = keyword == null ? "" : keyword.replace("\"", "").trim();

        // Retry page navigation (handles SPA Object doesn't exist errors)
        String searchUrl = getSearchUrl() + "&key=" + cleanKeyword;
        boolean navigated = false;
        for (int retry = 0; retry < 3; retry++) {
            try {
                page.navigate(searchUrl);
                navigated = true;
                break;
            } catch (Exception e) {
                if (isObjectNotExistError(e)) {
                    log.warn("Navigate failed (Object doesn't exist), retry {}/3: {}", retry + 1, e.getMessage());
                    PlaywrightUtil.sleep(2);
                } else {
                    throw e;
                }
            }
        }
        if (!navigated) {
            log.error("Navigate to search page failed after 3 retries");
            return;
        }

        // Wait for pagination (re-create locators to avoid stale DOM references)
        try {
            page.waitForSelector(PAGINATION_BOX, new Page.WaitForSelectorOptions().setTimeout(10000));
        } catch (Exception e) {
            log.warn("Wait for pagination element timed out: {}", e.getMessage());
        }
        setMaxPage(page.locator(PAGINATION_BOX).locator("li"));

        for (int i = 0; i < maxPage; i++) {
            if (shouldStop()) {
                info("Stop signal received, ending pagination loop");
                return;
            }
            try {
                // Try to close subscription popup
                Locator closeBtn = page.locator(SUBSCRIBE_CLOSE_BTN);
                if (closeBtn.count() > 0) {
                    closeBtn.click();
                }
            } catch (Exception ignored) {
            }

            // Wait for job cards to be attached (not visible, to avoid occlusion timeout)
            try {
                page.waitForSelector(
                    JOB_CARDS,
                    new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(15000)
                );
            } catch (Exception e) {
                log.warn("Wait for job cards timed out: {}", e.getMessage());
            }
            // Wait for API response to ensure lastApiEntities is refreshed
            try {
                page.waitForResponse(r -> {
                    try {
                        String u = r.url();
                        return u != null && u.contains("com.liepin.searchfront4c.pc-search-job") && r.status() == 200;
                    } catch (Exception ignored) { return false; }
                }, () -> {});
            } catch (Exception ignored) {}

            info(String.format("Delivering [%s] page [%d]...", cleanKeyword, i + 1));
            submitJob();
            info(String.format("Delivered page [%d], all jobs done.", i + 1));

            // Find next page button (re-create locator each time)
            try {
                Locator paginationBox = page.locator(PAGINATION_BOX);
                Locator nextLi = paginationBox.locator(NEXT_PAGE);
                if (nextLi.count() > 0) {
                    String cls = nextLi.first().getAttribute("class");
                    boolean disabled = cls != null && cls.contains("ant-pagination-disabled");
                    if (!disabled) {
                        Locator btn = nextLi.first().locator("button.ant-pagination-item-link");
                        if (btn.count() > 0) {
                            btn.first().click();
                        } else {
                            nextLi.first().click();
                        }
                        // Wait for page response before continuing
                        PlaywrightUtil.sleep(2);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.warn("Pagination failed: {}", e.getMessage());
                break;
            }
        }
        info(String.format("[%s] Keyword delivery completed!", cleanKeyword));
    }

    /** Check if it's a Playwright "Object doesn't exist" error */
    private boolean isObjectNotExistError(Exception e) {
        String msg = e.getMessage();
        return msg != null && msg.contains("Object doesn't exist");
    }

    private String getSearchUrl() {
        String baseUrl = "https://www.liepin.com/zhaopin/?";
        StringBuilder sb = new StringBuilder(baseUrl);
        // Directly concatenate params; ignore if empty
        if (config.getCityCode() != null && !config.getCityCode().isEmpty()) {
            sb.append("city=").append(config.getCityCode()).append("&");
            sb.append("dq=").append(config.getCityCode()).append("&");
        }
        if (config.getSalary() != null && !config.getSalary().isEmpty()) {
            sb.append("salary=").append(config.getSalary()).append("&");
        }
        sb.append("currentPage=0");
        return sb.toString();
    }

    private void setMaxPage(Locator lis) {
        try {
            int count = lis.count();
            if (count >= 2) {
                String pageText = lis.nth(count - 2).textContent();
                int page = Integer.parseInt(pageText);
                if (page > 1) {
                    maxPage = page;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void submitJob() {
        // Get job card count (with retry for SPA Object doesn't exist errors)
        int count = 0;
        for (int retry = 0; retry < 3; retry++) {
            try {
                count = page.locator(JOB_CARDS).count();
                break;
            } catch (Exception e) {
                if (isObjectNotExistError(e) && retry < 2) {
                    log.warn("Get job card count failed (Object doesn't exist), retry {}/3: {}", retry + 1, e.getMessage());
                    PlaywrightUtil.sleep(2);
                } else {
                    log.error("Get job card count failed: {}", e.getMessage());
                    return;
                }
            }
        }
        if (count == 0) {
            log.warn("No job cards on current page");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (shouldStop()) {
                info("Stop signal received, ending card iteration");
                return;
            }

            // Get display fields from API data
            String jobName = null;
            String companyName = null;
            String salary = null;
            String recruiterName = null;
            if (i < lastApiEntities.size()) {
                LiepinEntity apiEntity = lastApiEntities.get(i);
                jobName = safeText(apiEntity.getJobTitle());
                companyName = safeText(apiEntity.getCompName());
                salary = safeText(apiEntity.getJobSalaryText());
                recruiterName = safeText(apiEntity.getHrName());
            }
            if (recruiterName == null) recruiterName = "HR";
            if (jobName == null) jobName = "Job";
            if (companyName == null) companyName = "Company";
            if (salary == null) salary = "";

            // Re-create locator before each use (avoid stale references from SPA DOM updates)
            try {
                // Scroll to card position (via JS, avoids Playwright locator invalidation)
                try {
                    page.evaluate("(idx) => { var cards = document.querySelectorAll('div[class*=\"job-card-pc-container\"]'); if (cards[idx]) { cards[idx].scrollIntoView({behavior: 'instant', block: 'center'}); } }", i);
                } catch (Exception scrollError) {
                    log.debug("JS scroll failed: {}", scrollError.getMessage());
                }

                // Wait for button to render
                PlaywrightUtil.sleep(1);

                // Find chat button - use global selector + nth to avoid stale locator chain references
                Locator chatButton = null;
                for (int btnRetry = 0; btnRetry < 2; btnRetry++) {
                    try {
                        // Find chat button within card
                        chatButton = page.locator(JOB_CARDS).nth(i)
                            .locator("button:has-text('聊一聊')");
                        if (chatButton.count() > 0) {
                            break;
                        }
                        // Fallback: global search
                        chatButton = page.locator("button:has-text('聊一聊')").nth(i);
                        if (chatButton.count() > 0) {
                            break;
                        }
                    } catch (Exception e) {
                        if (btnRetry < 1) PlaywrightUtil.sleep(1);
                    }
                }

                if (chatButton != null && chatButton.count() > 0) {
                    try {
                        chatButton.first().click();
                        PlaywrightUtil.sleep(1);

                        // Wait for chat window and close
                        try {
                            page.waitForSelector(CHAT_HEADER, new Page.WaitForSelectorOptions().setTimeout(3000));
                            Locator close = page.locator(CHAT_CLOSE);
                            if (close.count() > 0) {
                                PlaywrightUtil.sleep(1);
                                close.click();
                            }
                        } catch (Exception e) {
                            log.debug("Close chat window failed: {}", e.getMessage());
                        }

                        // record success
                        resultList.add(String.format("[%s %s %s %s]", companyName, jobName, salary, recruiterName));
                        Long jobIdForUpdate = (i < lastApiEntities.size()) ? lastApiEntities.get(i).getJobId() : null;
                        if (jobIdForUpdate != null) {
                            liepinService.markDelivered(jobIdForUpdate);
                        }
                        info(String.format("Delivered: %s - %s (%s)", companyName, jobName, recruiterName));
                    } catch (Exception clickError) {
                        log.warn("Click chat button failed: {} - {} - {}: {}", companyName, jobName, recruiterName, clickError.getMessage());
                    }
                } else {
                    // Check if "continue chat" (already delivered)
                    try {
                        Locator continueBtn = page.locator(JOB_CARDS).nth(i)
                            .locator("button:has-text('继续聊')");
                        if (continueBtn.count() > 0) {
                            Long jobIdForUpdate = (i < lastApiEntities.size()) ? lastApiEntities.get(i).getJobId() : null;
                            if (jobIdForUpdate != null) {
                                liepinService.markDelivered(jobIdForUpdate);
                            }
                            log.debug("Job already delivered (continue chat): {} - {}", companyName, jobName);
                        } else {
                            log.debug("Chat button not found: {} - {}", companyName, jobName);
                        }
                    } catch (Exception e) {
                        log.debug("Check continue-chat button failed: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Processing card {} exception: {} - {}: {}", i, companyName, jobName, e.getMessage());
            }
        }
    }

    // Extract jobId from card data attributes (fallback when lastApiEntities is missing)
    private Long extractJobIdFromCard(Locator card) {
        try {
            String ext = card.getAttribute("data-tlg-ext");
            if (ext != null && !ext.isEmpty()) {
                try {
                    String decoded = java.net.URLDecoder.decode(ext, java.nio.charset.StandardCharsets.UTF_8);
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(decoded);
                    String jobIdStr = node.path("jobId").asText(null);
                    if (jobIdStr != null && !jobIdStr.isEmpty()) {
                        return Long.parseLong(jobIdStr);
                    }
                } catch (Exception ignore) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\\\"jobId\\\\\":\\\\\"(\\d+)\\\\\"").matcher(ext);
                    if (m.find()) {
                        return Long.parseLong(m.group(1));
                    }
                }
            }
            String scm = card.getAttribute("data-tlg-scm");
            if (scm != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("jobId=(\\d+)").matcher(scm);
                if (m.find()) {
                    return Long.parseLong(m.group(1));
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}
