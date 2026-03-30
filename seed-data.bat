@echo off
REM =====================================================
REM MetalCO ERP - Seed Data via API
REM Uses backend REST API to properly seed master data
REM =====================================================

set BASE=http://localhost:8089/api/metalco

REM --- Get auth token first ---
echo Getting auth token...
curl.exe -s -X POST %BASE%/auth/login -H "Content-Type: application/json" -d @login-test.json -o token_response.json

echo.
echo === SEEDING UNIT MASTER ===
curl.exe -s -X POST %BASE%/unit/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"unitAddress\":\"Plot 5 MIDC Industrial Area\",\"area\":\"Andheri East\",\"state\":\"Maharashtra\",\"country\":\"India\",\"pincode\":\"400001\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"27AABCU9603R1ZM\",\"gstStateCode\":\"27\",\"pan\":\"AABCU9603R\",\"typeOfEntity\":\"Private Limited\",\"status\":\"APPROVED\"}"
echo.

echo === SEEDING SUPPLIER MASTER ===
curl.exe -s -X POST %BASE%/supplier-master/save -H "Content-Type: application/json" -d "{\"supplierCode\":\"MESU0001\",\"supplierName\":\"NALCO ALUMINIUM LTD\",\"supplierCategory\":\"A\",\"supplierType\":\"Standard\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"21AABCN0001R1ZM\",\"gstStateCode\":\"21\",\"pan\":\"AABCN0001R\",\"brand\":\"NALCO\",\"typeOfEntity\":\"Public Limited\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/supplier-master/save -H "Content-Type: application/json" -d "{\"supplierCode\":\"MESU0002\",\"supplierName\":\"HINDALCO INDUSTRIES LTD\",\"supplierCategory\":\"A\",\"supplierType\":\"Standard\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"27AADCH0001R1ZM\",\"gstStateCode\":\"27\",\"pan\":\"AADCH0001R\",\"brand\":\"HINDALCO\",\"typeOfEntity\":\"Public Limited\",\"status\":\"APPROVED\"}"
echo.

echo === SEEDING CUSTOMER MASTER ===
curl.exe -s -X POST %BASE%/customer-master/save -H "Content-Type: application/json" -d "{\"customerCode\":\"MECU0001\",\"customerName\":\"AUTOPARTS INDIA LTD\",\"mailingBillingName\":\"AutoParts India Ltd\",\"customerCategory\":\"A\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"27AABCA0001R1ZM\",\"gstStateCode\":\"27\",\"pan\":\"AABCA0001R\",\"creditLimitAmount\":500000,\"creditLimitDays\":30,\"typeOfEntity\":\"Private Limited\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/customer-master/save -H "Content-Type: application/json" -d "{\"customerCode\":\"MECU0002\",\"customerName\":\"BUILDTECH SOLUTIONS PVT LTD\",\"mailingBillingName\":\"BuildTech Solutions\",\"customerCategory\":\"B\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"29AABCB0001R1ZM\",\"gstStateCode\":\"29\",\"pan\":\"AABCB0001R\",\"creditLimitAmount\":750000,\"creditLimitDays\":45,\"typeOfEntity\":\"Private Limited\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/customer-master/save -H "Content-Type: application/json" -d "{\"customerCode\":\"MECU0003\",\"customerName\":\"METALWORKS PVT LTD\",\"mailingBillingName\":\"MetalWorks Pvt Ltd\",\"customerCategory\":\"A\",\"gstRegistrationType\":\"Regular\",\"gstOrUin\":\"24AABCM0001R1ZM\",\"gstStateCode\":\"24\",\"pan\":\"AABCM0001R\",\"creditLimitAmount\":1000000,\"creditLimitDays\":60,\"typeOfEntity\":\"Private Limited\",\"status\":\"APPROVED\"}"
echo.

echo === SEEDING ITEM MASTER ===
curl.exe -s -X POST %BASE%/item-master/save -H "Content-Type: application/json" -d "{\"productCategory\":\"SHEET\",\"materialType\":\"Aluminium\",\"hsnCode\":\"7606\",\"sectionNumber\":\"SEC-001\",\"dimension1\":\"1\",\"dimension2\":\"1250\",\"dimension3\":\"2500\",\"dimension\":\"1X1250X2500\",\"grade\":\"1100\",\"temper\":\"H14\",\"brand\":\"NALCO\",\"skuDescription\":\"Aluminium Sheet 1mm NALCO 1100 H14\",\"supplierCode\":\"MESU0001\",\"supplierName\":\"NALCO ALUMINIUM LTD\",\"primaryUom\":\"KGS\",\"altUomApplicable\":\"Yes\",\"altUom\":\"NOS\",\"reportingUom\":\"KGS\",\"leadTimeDays\":15,\"moq\":500,\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"gstApplicable\":\"Yes\",\"gstRate\":18.00,\"itemPrice\":280.00,\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/item-master/save -H "Content-Type: application/json" -d "{\"productCategory\":\"COIL\",\"materialType\":\"Aluminium\",\"hsnCode\":\"7606\",\"sectionNumber\":\"SEC-002\",\"dimension1\":\"0.5\",\"dimension2\":\"1000\",\"dimension\":\"0.5X1000\",\"grade\":\"3003\",\"temper\":\"O\",\"brand\":\"HINDALCO\",\"skuDescription\":\"Aluminium Coil 0.5mm HINDALCO 3003 O\",\"supplierCode\":\"MESU0002\",\"supplierName\":\"HINDALCO INDUSTRIES LTD\",\"primaryUom\":\"KGS\",\"altUomApplicable\":\"Yes\",\"altUom\":\"NOS\",\"reportingUom\":\"KGS\",\"leadTimeDays\":20,\"moq\":1000,\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"gstApplicable\":\"Yes\",\"gstRate\":18.00,\"itemPrice\":265.00,\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/item-master/save -H "Content-Type: application/json" -d "{\"productCategory\":\"PLATE\",\"materialType\":\"Aluminium\",\"hsnCode\":\"7606\",\"sectionNumber\":\"SEC-003\",\"dimension1\":\"6\",\"dimension2\":\"1250\",\"dimension3\":\"2500\",\"dimension\":\"6X1250X2500\",\"grade\":\"6061\",\"temper\":\"T6\",\"brand\":\"NALCO\",\"skuDescription\":\"Aluminium Plate 6mm NALCO 6061 T6\",\"supplierCode\":\"MESU0001\",\"supplierName\":\"NALCO ALUMINIUM LTD\",\"primaryUom\":\"KGS\",\"altUomApplicable\":\"Yes\",\"altUom\":\"NOS\",\"reportingUom\":\"KGS\",\"leadTimeDays\":25,\"moq\":200,\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"gstApplicable\":\"Yes\",\"gstRate\":18.00,\"itemPrice\":350.00,\"status\":\"APPROVED\"}"
echo.

echo === SEEDING HSN CODE MASTER ===
curl.exe -s -X POST %BASE%/hsn-code-master/save -H "Content-Type: application/json" -d "{\"materialType\":\"Aluminium\",\"productCategory\":\"SHEET\",\"hsnCode\":\"7606\",\"effectiveDate\":\"2024-01-01\",\"gstRate\":\"18\",\"gstEffectiveDate\":\"2024-01-01\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/hsn-code-master/save -H "Content-Type: application/json" -d "{\"materialType\":\"Aluminium\",\"productCategory\":\"COIL\",\"hsnCode\":\"7606\",\"effectiveDate\":\"2024-01-01\",\"gstRate\":\"18\",\"gstEffectiveDate\":\"2024-01-01\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/hsn-code-master/save -H "Content-Type: application/json" -d "{\"materialType\":\"Aluminium\",\"productCategory\":\"PLATE\",\"hsnCode\":\"7606\",\"effectiveDate\":\"2024-01-01\",\"gstRate\":\"18\",\"gstEffectiveDate\":\"2024-01-01\",\"status\":\"APPROVED\"}"
echo.

echo === SEEDING PROCESS FLOW ===
curl.exe -s -X POST %BASE%/process-entry/save -H "Content-Type: application/json" -d "{\"processType\":\"Cutting\",\"operationType\":\"Primary\",\"mode\":\"Machine\"}"
echo.
curl.exe -s -X POST %BASE%/process-entry/save -H "Content-Type: application/json" -d "{\"processType\":\"Slitting\",\"operationType\":\"Primary\",\"mode\":\"Machine\"}"
echo.

echo === SEEDING MACHINE MASTER ===
curl.exe -s -X POST %BASE%/machine-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"machineId\":\"MCH-001\",\"machineName\":\"CNC Cutting Machine A1\",\"machineType\":\"Cutting\",\"modelNumber\":\"CNC-2500\",\"manufacturer\":\"Amada\",\"status\":\"APPROVED\"}"
echo.
curl.exe -s -X POST %BASE%/machine-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"machineId\":\"MCH-002\",\"machineName\":\"Hydraulic Slitter S1\",\"machineType\":\"Slitting\",\"modelNumber\":\"HS-1000\",\"manufacturer\":\"Schuler\",\"status\":\"APPROVED\"}"
echo.

echo === SEEDING RACK AND BIN MASTER ===
curl.exe -s -X POST %BASE%/rack-bin-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"storageType\":\"RM Inward\",\"storageArea\":\"Area-A\",\"rackNo\":\"R01\",\"columnNo\":\"C01\",\"binNo\":\"B01\",\"status\":\"available\",\"binCapacity\":\"5000 KG\"}"
echo.
curl.exe -s -X POST %BASE%/rack-bin-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"storageType\":\"RM Inward\",\"storageArea\":\"Area-A\",\"rackNo\":\"R01\",\"columnNo\":\"C02\",\"binNo\":\"B01\",\"status\":\"available\",\"binCapacity\":\"5000 KG\"}"
echo.
curl.exe -s -X POST %BASE%/rack-bin-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"storageType\":\"FG Store\",\"storageArea\":\"Area-FG\",\"rackNo\":\"R01\",\"columnNo\":\"C01\",\"binNo\":\"B01\",\"status\":\"available\",\"binCapacity\":\"5000 KG\"}"
echo.
curl.exe -s -X POST %BASE%/rack-bin-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"storageType\":\"QC Hold\",\"storageArea\":\"Area-QC\",\"rackNo\":\"R01\",\"columnNo\":\"C01\",\"binNo\":\"B01\",\"status\":\"available\",\"binCapacity\":\"2000 KG\"}"
echo.
curl.exe -s -X POST %BASE%/rack-bin-master/save -H "Content-Type: application/json" -d "{\"unitCode\":\"UNIT-01\",\"unitName\":\"MetalCo Unit 1 - Mumbai\",\"storageType\":\"Scrap\",\"storageArea\":\"Area-SC\",\"rackNo\":\"R01\",\"columnNo\":\"C01\",\"binNo\":\"B01\",\"status\":\"available\",\"binCapacity\":\"3000 KG\"}"
echo.

echo === SEEDING NALCO PRICE ===
curl.exe -s -X POST %BASE%/nalco-price/save -H "Content-Type: application/json" -d "{\"date\":\"2026-03-28\",\"nalcoPrice\":235.50,\"uom\":\"KGS\"}"
echo.

echo === DONE ===
echo All seed data has been created via API!
pause
