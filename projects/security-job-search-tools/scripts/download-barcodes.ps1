# 条形码下载脚本
$targetDir = "D:\360MoveData\Users\lx\Desktop\666"

# 提取三个URL中的条形码数据
$urls = @(
    "http://www.t-x-m.com/indexbarcode.asp?bc1=70800297%0D%0A70800304%0D%0A70800303%0D%0A70800012%0D%0A70800011%0D%0A70800306%0D%0A70800305%0D%0A70800302%0D%0A70800301%0D%0A70800300%0D%0A70800299%0D%0A70800154%0D%0A70800153%0D%0A70800150%0D%0A70800293%0D%0A70800292%0D%0A70800291%0D%0A70800290%0D%0A70800145%0D%0A70800144%0D%0A70800143%0D%0A70800142%0D%0A70800141%0D%0A70800140%0D%0A70800139%0D%0A70800138%0D%0A70800137%0D%0A70800001%0D%0A70800289%0D%0A70800288%0D%0A70800287%0D%0A70800286%0D%0A70800285%0D%0A70800296%0D%0A70800295%0D%0A70800284%0D%0A70800283%0D%0A70800294%0D%0A70800281%0D%0A70800282%0D%0A71900005%0D%0A71900006%0D%0A71700189%0D%0A71700190%0D%0A71700191%0D%0A71700192%0D%0A71700194%0D%0A71700195%0D%0A71700196%0D%0A71700197%0D%0A71700198%0D%0A71700199%0D%0A71700200%0D%0A71700201%0D%0A71900001%0D%0A71900002%0D%0A71900003%0D%0A71700174%0D%0A71700175%0D%0A71700176%0D%0A71700177%0D%0A71700178%0D%0A71700179%0D%0A71700180%0D%0A71700181%0D%0A71700182%0D%0A71700183%0D%0A71700166%0D%0A71700167%0D%0A71700168%0D%0A71700151%0D%0A71700152%0D%0A71700154%0D%0A71700155%0D%0A71700156%0D%0A71700157%0D%0A71700149%0D%0A71700158%0D%0A71700170%0D%0A71700171%0D%0A71700172%0D%0A71700165%0D%0A71700164%0D%0A71700162%0D%0A71700161%0D%0A71700160%0D%0A70802452%0D%0A79900266%0D%0A79900496%0D%0A79900418%0D%0A70800421%0D%0A70800423%0D%0A70800424%0D%0A70800416%0D%0A70800417%0D%0A70800418%0D%0A70800419%0D%0A70800420%0D%0A70800422%0D%0A70800410%0D%0A&bc2=10&bc3=3.5&bc4=1.2&bc9=1&bc5=11&bc6=11&bc7=Arial&bc8=15",
    "http://www.t-x-m.com/indexbarcode.asp?bc1=70800409%0D%0A70800412%0D%0A70800413%0D%0A70800414%0D%0A70800415%0D%0A70800546%0D%0A70800547%0D%0A70800548%0D%0A70800549%0D%0A70800550%0D%0A70800407%0D%0A70800408%0D%0A70800411%0D%0A70800399%0D%0A70800400%0D%0A70800401%0D%0A70800402%0D%0A70800403%0D%0A70800404%0D%0A70800405%0D%0A70800406%0D%0A70800544%0D%0A70800545%0D%0A70800534%0D%0A70800535%0D%0A70800536%0D%0A70800538%0D%0A70800539%0D%0A70800540%0D%0A70800541%0D%0A70800521%0D%0A70800530%0D%0A70800531%0D%0A70800532%0D%0A70800502%0D%0A70800503%0D%0A70800505%0D%0A70800506%0D%0A70800509%0D%0A70800513%0D%0A70800514%0D%0A70800515%0D%0A71700054%0D%0A71700055%0D%0A70800516%0D%0A70800517%0D%0A70800518%0D%0A70800519%0D%0A70800520%0D%0A70800498%0D%0A70800499%0D%0A70800501%0D%0A70800331%0D%0A70800332%0D%0A70800333%0D%0A70800334%0D%0A70800475%0D%0A70800476%0D%0A70800477%0D%0A71700014%0D%0A71700015%0D%0A71700016%0D%0A70800335%0D%0A70800336%0D%0A70800337%0D%0A70800338%0D%0A70800340%0D%0A70800341%0D%0A70800342%0D%0A70800343%0D%0A70800327%0D%0A70800329%0D%0A70800330%0D%0A71700147%0D%0A71700148%0D%0A71700145%0D%0A70800461%0D%0A70800323%0D%0A70800326%0D%0A70800470%0D%0A70800468%0D%0A70800467%0D%0A70800466%0D%0A70800465%0D%0A70800464%0D%0A70800463%0D%0A70800462%0D%0A70800320%0D%0A70800319%0D%0A70800318%0D%0A70800460%0D%0A70800457%0D%0A70800456%0D%0A70800454%0D%0A71700146%0D%0A71700134%0D%0A70800436%0D%0A70800435%0D%0A70800434%0D%0A70800433%0D%0A&bc2=10&bc3=3.5&bc4=1.2&bc9=1&bc5=11&bc6=11&bc7=Arial&bc8=15",
    "http://www.t-x-m.com/indexbarcode.asp?bc1=70800432%0D%0A70800431%0D%0A70800430%0D%0A70800429%0D%0A70800428%0D%0A70800427%0D%0A70800426%0D%0A70800425%0D%0A71700144%0D%0A71700142%0D%0A71700141%0D%0A71700140%0D%0A70800449%0D%0A70800448%0D%0A71700138%0D%0A71700135%0D%0A71700136%0D%0A71700137%0D%0A71700112%0D%0A71700113%0D%0A70802328%0D%0A70802327%0D%0A70802453%0D%0A80800387%0D%0A80800379%0D%0A80800380%0D%0A80800381%0D%0A80800382%0D%0A80800383%0D%0A80800384%0D%0A80800385%0D%0A80800386%0D%0A80800368%0D%0A80800369%0D%0A80800370%0D%0A80800371%0D%0A80800372%0D%0A80800373%0D%0A80800374%0D%0A80800375%0D%0A80800376%0D%0A80800377%0D%0A80800378%0D%0A81000088%0D%0A81000087%0D%0A81000086%0D%0A81000029%0D%0A&bc2=10&bc3=3.5&bc4=1.2&bc9=1&bc5=11&bc6=11&bc7=Arial&bc8=15"
)

# 从URL中提取条形码数据的函数
function Extract-Barcodes($url, $pageNum) {
    $barcodes = @()
    
    # 解析URL获取bc1参数
    $uri = [System.Uri]$url
    $queryParams = [System.Web.HttpUtility]::ParseQueryString($uri.Query)
    $bc1Value = $queryParams["bc1"]
    
    if ($bc1Value) {
        # 分割条形码（用回车换行分隔）
        $barcodeList = $bc1Value -split "`r`n" | Where-Object { $_ -ne "" }
        
        foreach ($barcode in $barcodeList) {
            $barcodes += @{
                Code = $barcode
                Page = $pageNum
            }
        }
    }
    
    return $barcodes
}

Write-Host "开始提取条形码..."

# 提取所有条形码
$allBarcodes = @()
$pageNum = 1

foreach ($url in $urls) {
    $barcodes = Extract-Barcodes $url $pageNum
    $allBarcodes += $barcodes
    Write-Host "页面 $pageNum 提取到 $($barcodes.Count) 个条形码"
    $pageNum++
}

Write-Host "总共提取到 $($allBarcodes.Count) 个条形码"

# 现在下载条形码 - 这个网站通常有个生成图片的接口
# 让我先尝试下载第一张图片来看看接口
$testBarcode = $allBarcodes[0]
Write-Host "测试下载: $($testBarcode.Code)"

# 尝试不同的可能的图片URL格式
$possibleImageUrls = @(
    "http://www.t-x-m.com/barcode.asp?bc=$($testBarcode.Code)",
    "http://www.t-x-m.com/image.asp?code=$($testBarcode.Code)",
    "http://www.t-x-m.com/generate.asp?code=$($testBarcode.Code)"
)

$foundImageUrl = $null
foreach ($imgUrl in $possibleImageUrls) {
    try {
        $testFile = "$targetDir\test-$($testBarcode.Code).png"
        Invoke-WebRequest -Uri $imgUrl -OutFile $testFile -UseBasicParsing -TimeoutSec 10
        
        if (Test-Path $testFile) {
            $fileInfo = Get-Item $testFile
            if ($fileInfo.Length -gt 0) {
                Write-Host "找到有效图片URL: $imgUrl"
                $foundImageUrl = $imgUrl
                Remove-Item $testFile -Force
                break
            } else {
                Remove-Item $testFile -Force
            }
        }
    } catch {
        Write-Host "尝试失败: $imgUrl"
    }
}

if (-not $foundImageUrl) {
    Write-Host "无法找到图片生成接口。让我尝试直接访问页面并解析HTML中的图片..."
    
    # 让我创建一个更简单的方法 - 访问每个条形码页面
    $counter = 1
    foreach ($barcode in $allBarcodes) {
        $barcodeNum = $barcode.Code
        $outputFile = Join-Path $targetDir ("barcode_{0:D3}_{1}.png" -f $counter, $barcodeNum)
        
        # 构建包含单个条形码的URL
        $singleBarcodeUrl = "http://www.t-x-m.com/indexbarcode.asp?bc1=$([Uri]::EscapeDataString($barcodeNum))&bc2=10&bc3=3.5&bc4=1.2&bc9=1&bc5=11&bc6=11&bc7=Arial&bc8=15"
        
        Write-Host "[$counter/$($allBarcodes.Count)] 下载: $barcodeNum"
        
        try {
            # 获取页面内容
            $response = Invoke-WebRequest -Uri $singleBarcodeUrl -UseBasicParsing -TimeoutSec 30
            
            # 尝试从HTML中提取图片
            $html = $response.Content
            
            # 查找img标签
            if ($html -match '<img[^>]*src=["'']([^"'']+)["'']') {
                $imgSrc = $matches[1]
                
                # 处理相对URL
                if ($imgSrc -notlike 'http*') {
                    if ($imgSrc -like '/*') {
                        $imgUrl = "http://www.t-x-m.com$imgSrc"
                    } else {
                        $imgUrl = "http://www.t-x-m.com/$imgSrc"
                    }
                } else {
                    $imgUrl = $imgSrc
                }
                
                Write-Host "  找到图片URL: $imgUrl"
                
                # 下载图片
                Invoke-WebRequest -Uri $imgUrl -OutFile $outputFile -UseBasicParsing -TimeoutSec 30
                
                if (Test-Path $outputFile) {
                    $fileInfo = Get-Item $outputFile
                    Write-Host "  下载完成: $([math]::Round($fileInfo.Length/1KB, 2)) KB"
                }
            }
        } catch {
            Write-Host "  下载失败: $_"
        }
        
        $counter++
        
        # 稍微延迟避免请求过快
        Start-Sleep -Milliseconds 200
    }
} else {
    Write-Host "使用找到的图片URL进行批量下载..."
    
    $counter = 1
    foreach ($barcode in $allBarcodes) {
        $barcodeNum = $barcode.Code
        $outputFile = Join-Path $targetDir ("barcode_{0:D3}_{1}.png" -f $counter, $barcodeNum)
        
        # 替换条形码
        $imgUrl = $foundImageUrl.Replace($testBarcode.Code, $barcodeNum)
        
        Write-Host "[$counter/$($allBarcodes.Count)] 下载: $barcodeNum"
        
        try {
            Invoke-WebRequest -Uri $imgUrl -OutFile $outputFile -UseBasicParsing -TimeoutSec 30
            
            if (Test-Path $outputFile) {
                $fileInfo = Get-Item $outputFile
                Write-Host "  下载完成: $([math]::Round($fileInfo.Length/1KB, 2)) KB"
            }
        } catch {
            Write-Host "  下载失败: $_"
        }
        
        $counter++
        
        # 稍微延迟避免请求过快
        Start-Sleep -Milliseconds 200
    }
}

Write-Host "下载完成！文件保存在: $targetDir"

# 统计下载的文件
$downloadedFiles = Get-ChildItem -Path $targetDir -Filter "*.png" | Measure-Object
Write-Host "总共下载了 $($downloadedFiles.Count) 个文件"
