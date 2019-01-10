chcp 850
cd "%~dp0"
java -Xms1024m -jar "%~dp0\Resip.exe" -h -x  -c "%~dp0\Config\ExportContext.config" -w "%~dp0\Logs" -d "%~f1" -g "%~dp0\SipOutput.zip" -v STEP
pause