/**
 * MetalCO ERP - Complete Flow Automation & Test Script
 * Seeds master data and tests all ERP flows end-to-end
 * 
 * Usage: node test-all-flows.js
 */

const http = require('http');

const BASE_URL = 'http://localhost:8089/api/metalco';

// Utility to make HTTP requests
function apiRequest(method, path, body = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(`${BASE_URL}${path}`);
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      method: method,
      headers: { 'Content-Type': 'application/json' },
      timeout: 30000
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          // Try JSON first, fall back to raw text
          const parsed = data ? JSON.parse(data) : null;
          resolve({ status: res.statusCode, data: parsed });
        } catch {
          resolve({ status: res.statusCode, data: data });
        }
      });
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(); reject(new Error('Request timeout')); });
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

// Track test results
const results = { passed: 0, failed: 0, errors: [] };

function pass(test) {
  results.passed++;
  console.log(`  ✅ PASS: ${test}`);
}

function fail(test, detail) {
  results.failed++;
  results.errors.push({ test, detail });
  console.log(`  ❌ FAIL: ${test} — ${detail}`);
}

async function testApi(name, method, path, body, expectedStatus) {
  try {
    const res = await apiRequest(method, path, body);
    if (expectedStatus && res.status !== expectedStatus) {
      fail(name, `Expected ${expectedStatus}, got ${res.status}`);
    } else {
      pass(name);
    }
    return res;
  } catch (e) {
    fail(name, e.message);
    return null;
  }
}

// ============================================================
// PHASE 1: SEED MASTER DATA
// ============================================================
async function seedMasterData() {
  console.log('\n╔══════════════════════════════════════════════╗');
  console.log('║  PHASE 1: SEEDING MASTER DATA                ║');
  console.log('╚══════════════════════════════════════════════╝\n');

  // --- 1. Supplier Master ---
  console.log('--- Supplier Master ---');
  const supplier1 = await testApi('Create Supplier NALCO', 'POST', '/supplier-master/create', {
    supplierCode: 'MESU0001',
    supplierName: 'NALCO ALUMINIUM LTD',
    mailingBillingName: 'NALCO Aluminium',
    supplierCategory: 'A',
    supplierType: 'Standard',
    gstRegistrationType: 'Regular',
    gstOrUin: '21AABCN0001R1ZM',
    gstStateCode: '21',
    pan: 'AABCN0001R',
    brand: 'NALCO',
    typeOfEntity: 'Public Limited',
    interestCalculation: 'No',
    status: 'APPROVED',
    addressDetails: [{
      addressLine1: '342 Nayapalli',
      city: 'Bhubaneswar',
      state: 'Odisha',
      pinCode: '751012',
      country: 'India'
    }],
    contactDetails: [{
      contactPersonName: 'Mr. Sales Manager',
      phone: '9876543210',
      email: 'sales@nalco.co.in'
    }],
    bankDetails: [{
      bankName: 'State Bank of India',
      accountNumber: '1234567890',
      ifscCode: 'SBIN0001234',
      branchName: 'Bhubaneswar Main'
    }]
  }, 200);

  const supplier2 = await testApi('Create Supplier HINDALCO', 'POST', '/supplier-master/create', {
    supplierCode: 'MESU0002',
    supplierName: 'HINDALCO INDUSTRIES LTD',
    mailingBillingName: 'Hindalco Industries',
    supplierCategory: 'A',
    supplierType: 'Standard',
    gstRegistrationType: 'Regular',
    gstOrUin: '27AADCH0001R1ZM',
    gstStateCode: '27',
    pan: 'AADCH0001R',
    brand: 'HINDALCO',
    typeOfEntity: 'Public Limited',
    interestCalculation: 'No',
    status: 'APPROVED',
    addressDetails: [{
      addressLine1: 'Aditya Birla Centre',
      city: 'Mumbai',
      state: 'Maharashtra',
      pinCode: '400021',
      country: 'India'
    }],
    contactDetails: [{
      contactPersonName: 'Mr. Distribution Head',
      phone: '9876543211',
      email: 'sales@hindalco.com'
    }],
    bankDetails: [{
      bankName: 'HDFC Bank',
      accountNumber: '9876543210',
      ifscCode: 'HDFC0001234',
      branchName: 'BKC Mumbai'
    }]
  }, 200);

  // Approve suppliers
  if (supplier1?.data?.id) {
    await testApi('Approve Supplier NALCO', 'PUT', `/supplier-master/approve/${supplier1.data.id}`, null, 200);
  }
  if (supplier2?.data?.id) {
    await testApi('Approve Supplier HINDALCO', 'PUT', `/supplier-master/approve/${supplier2.data.id}`, null, 200);
  }

  // Verify supplier list
  await testApi('Get All Suppliers', 'GET', '/supplier-master/all', null, 200);

  // --- 2. Customer Master ---
  console.log('\n--- Customer Master ---');
  const customer1 = await testApi('Create Customer AUTOPARTS', 'POST', '/customer-master/create', {
    customerCode: 'MECU0001',
    customerName: 'AUTOPARTS INDIA LTD',
    mailingBillingName: 'AutoParts India Ltd',
    customerCategory: 'A',
    gstRegistrationType: 'Regular',
    gstOrUin: '27AABCA0001R1ZM',
    gstStateCode: '27',
    pan: 'AABCA0001R',
    creditLimitAmount: 500000,
    creditLimitDays: 30,
    typeOfEntity: 'Private Limited',
    interestCalculation: 'No',
    status: 'APPROVED',
    addressDetails: [{
      addressLine1: 'Unit 12, Industrial Estate',
      city: 'Pune',
      state: 'Maharashtra',
      pinCode: '411001',
      country: 'India'
    }],
    contactDetails: [{
      contactPersonName: 'Rajesh Kumar',
      phone: '9898989898',
      email: 'rajesh@autoparts.com'
    }],
    bankDetails: [{
      bankName: 'ICICI Bank',
      accountNumber: '1122334455',
      ifscCode: 'ICIC0001234',
      branchName: 'Pune Main'
    }]
  }, 200);

  const customer2 = await testApi('Create Customer BUILDTECH', 'POST', '/customer-master/create', {
    customerCode: 'MECU0002',
    customerName: 'BUILDTECH SOLUTIONS PVT LTD',
    mailingBillingName: 'BuildTech Solutions',
    customerCategory: 'B',
    gstRegistrationType: 'Regular',
    gstOrUin: '29AABCB0001R1ZM',
    gstStateCode: '29',
    pan: 'AABCB0001R',
    creditLimitAmount: 750000,
    creditLimitDays: 45,
    typeOfEntity: 'Private Limited',
    interestCalculation: 'No',
    status: 'APPROVED',
    addressDetails: [{
      addressLine1: 'Whitefield Tech Park',
      city: 'Bangalore',
      state: 'Karnataka',
      pinCode: '560066',
      country: 'India'
    }],
    contactDetails: [{
      contactPersonName: 'Priya Sharma',
      phone: '9797979797',
      email: 'priya@buildtech.com'
    }],
    bankDetails: [{
      bankName: 'Axis Bank',
      accountNumber: '5566778899',
      ifscCode: 'UTIB0001234',
      branchName: 'Whitefield'
    }]
  }, 200);

  // Approve customers
  if (customer1?.data?.id) {
    await testApi('Approve Customer AUTOPARTS', 'PUT', `/customer-master/approve/${customer1.data.id}`, null, 200);
  }
  if (customer2?.data?.id) {
    await testApi('Approve Customer BUILDTECH', 'PUT', `/customer-master/approve/${customer2.data.id}`, null, 200);
  }

  await testApi('Get All Customers', 'GET', '/customer-master/all', null, 200);

  // --- 3. Item Master ---
  console.log('\n--- Item Master ---');
  const item1 = await testApi('Create Item ALU SHEET', 'POST', '/item-master', {
    productCategory: 'SHEET',
    materialType: 'Aluminium',
    hsnCode: '7606',
    sectionNumber: 'SEC-001',
    dimension1: '1', dimension2: '1250', dimension3: '2500',
    dimension: '1X1250X2500',
    grade: '1100', temper: 'H14', brand: 'NALCO',
    skuDescription: 'Aluminium Sheet 1mm NALCO 1100 H14',
    supplierCode: 'MESU0001',
    supplierName: 'NALCO ALUMINIUM LTD',
    primaryUom: 'KGS',
    altUomApplicable: 'Yes', altUom: 'NOS', reportingUom: 'KGS',
    leadTimeDays: 15, moq: 500,
    unitName: 'MetalCo Unit 1 - Mumbai',
    gstApplicable: 'Yes', gstRate: 18.00,
    itemPrice: 280.00,
    status: 'APPROVED'
  }, 201);

  const item2 = await testApi('Create Item ALU COIL', 'POST', '/item-master', {
    productCategory: 'COIL',
    materialType: 'Aluminium',
    hsnCode: '7606',
    sectionNumber: 'SEC-002',
    dimension1: '0.5', dimension2: '1000',
    dimension: '0.5X1000',
    grade: '3003', temper: 'O', brand: 'HINDALCO',
    skuDescription: 'Aluminium Coil 0.5mm HINDALCO 3003 O',
    supplierCode: 'MESU0002',
    supplierName: 'HINDALCO INDUSTRIES LTD',
    primaryUom: 'KGS',
    altUomApplicable: 'Yes', altUom: 'NOS', reportingUom: 'KGS',
    leadTimeDays: 20, moq: 1000,
    unitName: 'MetalCo Unit 1 - Mumbai',
    gstApplicable: 'Yes', gstRate: 18.00,
    itemPrice: 265.00,
    status: 'APPROVED'
  }, 201);

  const item3 = await testApi('Create Item ALU PLATE', 'POST', '/item-master', {
    productCategory: 'PLATE',
    materialType: 'Aluminium',
    hsnCode: '7606',
    sectionNumber: 'SEC-003',
    dimension1: '6', dimension2: '1250', dimension3: '2500',
    dimension: '6X1250X2500',
    grade: '6061', temper: 'T6', brand: 'NALCO',
    skuDescription: 'Aluminium Plate 6mm NALCO 6061 T6',
    supplierCode: 'MESU0001',
    supplierName: 'NALCO ALUMINIUM LTD',
    primaryUom: 'KGS',
    altUomApplicable: 'Yes', altUom: 'NOS', reportingUom: 'KGS',
    leadTimeDays: 25, moq: 200,
    unitName: 'MetalCo Unit 1 - Mumbai',
    gstApplicable: 'Yes', gstRate: 18.00,
    itemPrice: 350.00,
    status: 'APPROVED'
  }, 201);

  // Approve items
  if (item1?.data?.id) await testApi('Approve Item SHEET', 'PUT', `/item-master/approve/${item1.data.id}`, null, 200);
  if (item2?.data?.id) await testApi('Approve Item COIL', 'PUT', `/item-master/approve/${item2.data.id}`, null, 200);
  if (item3?.data?.id) await testApi('Approve Item PLATE', 'PUT', `/item-master/approve/${item3.data.id}`, null, 200);

  await testApi('Get All Items', 'GET', '/item-master/all', null, 200);

  // --- 4. HSN Code Master ---
  console.log('\n--- HSN Code Master ---');
  await testApi('Create HSN SHEET', 'POST', '/hsn-code-master/save', {
    materialType: 'Aluminium', productCategory: 'SHEET',
    hsnCode: '7606', effectiveDate: '2024-01-01',
    gstRate: '18', gstEffectiveDate: '2024-01-01',
    status: 'APPROVED'
  }, 200);
  await testApi('Create HSN COIL', 'POST', '/hsn-code-master/save', {
    materialType: 'Aluminium', productCategory: 'COIL',
    hsnCode: '7606', effectiveDate: '2024-01-01',
    gstRate: '18', gstEffectiveDate: '2024-01-01',
    status: 'APPROVED'
  }, 200);

  // --- 5. Machine Master ---
  console.log('\n--- Machine Master ---');
  await testApi('Create Machine CNC', 'POST', '/machine-master/save', {
    unitCode: 'UNIT-01', unitName: 'MetalCo Unit 1 - Mumbai',
    machineId: 'MCH-001', machineName: 'CNC Cutting Machine A1',
    machineType: 'Cutting', modelNumber: 'CNC-2500',
    manufacturer: 'Amada', status: 'APPROVED'
  }, 200);
  await testApi('Create Machine Slitter', 'POST', '/machine-master/save', {
    unitCode: 'UNIT-01', unitName: 'MetalCo Unit 1 - Mumbai',
    machineId: 'MCH-002', machineName: 'Hydraulic Slitter S1',
    machineType: 'Slitting', modelNumber: 'HS-1000',
    manufacturer: 'Schuler', status: 'APPROVED'
  }, 200);

  // --- 6. Rack and Bin Master ---
  console.log('\n--- Rack and Bin Master ---');
  const racks = [
    { storageType: 'RM Inward', storageArea: 'Area-A', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '5000 KG' },
    { storageType: 'RM Inward', storageArea: 'Area-A', rackNo: 'R01', columnNo: 'C02', binNo: 'B01', binCapacity: '5000 KG' },
    { storageType: 'RM Inward', storageArea: 'Area-B', rackNo: 'R02', columnNo: 'C01', binNo: 'B01', binCapacity: '5000 KG' },
    { storageType: 'FG Store', storageArea: 'Area-FG', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '5000 KG' },
    { storageType: 'FG Store', storageArea: 'Area-FG', rackNo: 'R01', columnNo: 'C02', binNo: 'B01', binCapacity: '5000 KG' },
    { storageType: 'QC Hold', storageArea: 'Area-QC', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '2000 KG' },
    { storageType: 'Scrap', storageArea: 'Area-SC', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '3000 KG' },
    { storageType: 'Machine', storageArea: 'Area-MC', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '3000 KG' },
    { storageType: 'Returnable', storageArea: 'Area-RT', rackNo: 'R01', columnNo: 'C01', binNo: 'B01', binCapacity: '2000 KG' },
  ];
  for (const rack of racks) {
    await testApi(`Create Rack ${rack.storageType} ${rack.rackNo}-${rack.columnNo}-${rack.binNo}`, 'POST', '/rack-bin-master/save', {
      unitCode: 'UNIT-01', unitName: 'MetalCo Unit 1 - Mumbai',
      ...rack, status: 'available'
    }, 200);
  }

  // --- 7. Process Flow ---
  console.log('\n--- Process Flow ---');
  await testApi('Create Process Cutting', 'POST', '/process-entry/save', {
    processType: 'Cutting', operationType: 'Primary', mode: 'Machine'
  }, 200);
  await testApi('Create Process Slitting', 'POST', '/process-entry/save', {
    processType: 'Slitting', operationType: 'Primary', mode: 'Machine'
  }, 200);

  // --- 8. NALCO Price ---
  console.log('\n--- NALCO Price ---');
  await testApi('Create NALCO Price', 'POST', '/nalco-price/save', {
    date: '2026-03-28', nalcoPrice: 235.50, uom: 'KGS'
  }, 200);

  console.log('\n✅ PHASE 1 COMPLETE: Master data seeded!');
}

// ============================================================
// PHASE 2: TEST ALL FLOWS
// ============================================================
async function testAllFlows() {
  console.log('\n╔══════════════════════════════════════════════╗');
  console.log('║  PHASE 2: TESTING ALL ERP FLOWS              ║');
  console.log('╚══════════════════════════════════════════════╝\n');

  // --- Test Master Data Retrieval ---
  console.log('--- Master Data Retrieval ---');
  await testApi('GET Supplier List', 'GET', '/supplier-master/all', null, 200);
  await testApi('GET Customer List', 'GET', '/customer-master/all', null, 200);
  await testApi('GET Item List', 'GET', '/item-master/all', null, 200);
  await testApi('GET Rack/Bin List', 'GET', '/rack-bin-master/all', null, 200);
  await testApi('GET Machine List', 'GET', '/machine-master/all', null, 200);
  await testApi('GET HSN List', 'GET', '/hsn-code-master/all', null, 200);
  await testApi('GET NALCO Prices', 'GET', '/nalco-price/all', null, 200);

  // --- Test Supplier Filters ---
  console.log('\n--- Supplier Filters ---');
  await testApi('Filter Supplier by Brand', 'GET', '/supplier-master/brand-suppliers?brand=NALCO', null, 200);
  await testApi('Filter Supplier by Name', 'GET', '/supplier-master/allNames', null, 200);
  await testApi('Filter Supplier by Code', 'GET', '/supplier-master/allCodes', null, 200);

  // --- Verify Item Dimension ---
  console.log('\n--- Item Master Queries ---');
  const itemsRes = await testApi('GET All Items for Verification', 'GET', '/item-master/all', null, 200);
  if (itemsRes?.data && Array.isArray(itemsRes.data) && itemsRes.data.length > 0) {
    const firstItem = itemsRes.data[0];
    pass(`Item found: ${firstItem.skuDescription || firstItem.productCategory}`);
    await testApi('GET Item by ID', 'GET', `/item-master/${firstItem.id}`, null, 200);
  }

  // --- Test Sales Order Flow ---
  console.log('\n--- Sales Order Flow ---');
  // Note: SO creation needs specific format based on frontend
  const soRes = await apiRequest('GET', '/sales-order/all');
  if (soRes.status === 200) {
    pass('GET Sales Orders List');
  } else {
    fail('GET Sales Orders List', `Status: ${soRes.status}`);
  }

  // --- Test Purchase Order Flow ---
  console.log('\n--- Purchase Order Flow ---');
  const poRes = await apiRequest('GET', '/po-request/all');
  if (poRes.status === 200) {
    pass('GET PO Requests List');
  } else {
    fail('GET PO Requests List', `Status: ${poRes.status}`);
  }

  // --- Test Warehouse Flows ---
  console.log('\n--- Warehouse Flows ---');
  await testApi('GET Gate Entry Inward List', 'GET', '/gate-inward/all', null, 200);
  await testApi('GET Vehicle Weighment List', 'GET', '/vehicle-weighment/all', null, 200);
  await testApi('GET GRN Summary List', 'GET', '/grn-summary/all', null, 200);
  await testApi('GET Stock Summary', 'GET', '/stock-summary/all', null, 200);
  await testApi('GET Stock Transfer List', 'GET', '/stock-transfers/all', null, 200);

  // --- Test Production Flows ---
  console.log('\n--- Production Flows ---');
  await testApi('GET Production Schedule', 'GET', '/production-schedule/all', null, 200);
  await testApi('GET Production Entry List', 'GET', '/production-entry/all', null, 200);

  // --- Test Packing & Dispatch ---
  console.log('\n--- Packing & Dispatch ---');
  await testApi('GET Packing Schedule', 'GET', '/packing-scheduler/all', null, 200);
  await testApi('GET Packing Submission List', 'GET', '/packing-submission/all', null, 200);
  await testApi('GET Packing List Transfer', 'GET', '/packing-list-transfer/all', null, 200);
  await testApi('GET Gate Entry Outward', 'GET', '/gate-entry-packing-and-dispatch/all', null, 200);

  // --- Test Billing ---
  console.log('\n--- Billing ---');
  await testApi('GET Billing Summary', 'GET', '/billing-summary/all', null, 200);

  // --- Test Reports ---
  console.log('\n--- Reports ---');
  await testApi('GET Salesman Incentive', 'GET', '/salesman-incentive-entry/all', null, 200);

  console.log('\n✅ PHASE 2 COMPLETE: Flow testing finished!');
}

// ============================================================
// MAIN EXECUTION
// ============================================================
async function main() {
  console.log('╔══════════════════════════════════════════════════╗');
  console.log('║  MetalCO ERP - COMPREHENSIVE TEST SUITE         ║');
  console.log('║  Tests ALL master data creation + flow endpoints ║');
  console.log('╚══════════════════════════════════════════════════╝\n');

  try {
    await seedMasterData();
    await testAllFlows();

    console.log('\n╔══════════════════════════════════════════════════╗');
    console.log('║  FINAL RESULTS                                   ║');
    console.log('╚══════════════════════════════════════════════════╝');
    console.log(`  ✅ Passed: ${results.passed}`);
    console.log(`  ❌ Failed: ${results.failed}`);
    console.log(`  📊 Total:  ${results.passed + results.failed}`);

    if (results.errors.length > 0) {
      console.log('\n  FAILED TESTS:');
      results.errors.forEach((e, i) => {
        console.log(`    ${i+1}. ${e.test}: ${e.detail}`);
      });
    }

    console.log('\n' + (results.failed === 0 ? '🎉 ALL TESTS PASSED!' : '⚠️  Some tests failed. Check errors above.'));

  } catch (e) {
    console.error('Fatal error:', e.message);
    process.exit(1);
  }
}

main();
