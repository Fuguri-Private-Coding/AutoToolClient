@echo off

set /p fontname="font file name (with file extension): "
cls

set /p size="size: "
cls

set /p pxrange="pxRange: "
cls

msdf.exe -font %fontname% -charset charset.txt -type mtsdf -format png -imageout atlas.png -json data.json -size %size% -square4 -pxrange %pxrange%
pause