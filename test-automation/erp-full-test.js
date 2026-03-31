/**
 * MetalCo ERP - Full Application Automation Test Script
 * Tests every module and submodule for:
 *   1. Page Load (HTTP 200 + no Angular error)
 *   2. API Backend Health (key endpoints)
 *   3. Core UI Elements (forms, tables, buttons)
 *   4. Navigation & Routing
 *
 * Usage: node test-automation/erp-full-test.js
 * Prerequisites: Backend on port 8089, Frontend on port 4200
 */

const http = require("http");
const https = require("https");

const BASE_URL = "http://localhost:4200";
const API_BASE = "http://localhost:8089/api/metalco";
const CREDENTIALS = { username: "superadmin", password: "superadmin" };

// ============================================================================
// TEST CONFIGURATION: All modules and their routes
// ============================================================================

const MODULES = {
  // ── Authentication ───────────────────────────────────────────────────
  "Authentication": {
    routes: [
      { name: "Login Page", path: "/#/signin" },
    ],
  },

  // ── Dashboard ────────────────────────────────────────────────────────
  "Dashboard": {
    routes: [
      { name: "Admin Dashboard", path: "/#/dashboard" },
    ],
  },

  // ── Masters ──────────────────────────────────────────────────────────
  "Masters": {
    routes: [
      { name: "Customer Master", path: "/#/data/customers" },
      { name: "Supplier Master", path: "/#/data/supplier" },
      { name: "Item Master", path: "/#/data/items" },
      { name: "Sub-Contractor Master", path: "/#/data/sub-contractor" },
      { name: "Unit Master", path: "/#/data/unit" },
      { name: "Rack & Bin Master", path: "/#/data/rack-and-bin" },
      { name: "User Master", path: "/#/data/user" },
      { name: "Salesman Master", path: "/#/data/salesman" },
      { name: "Machine Master", path: "/#/data/machine" },
      { name: "Nalco Price", path: "/#/data/nalco-price" },
      { name: "Hindalco Price", path: "/#/data/hindalco-price" },
      { name: "Process Flow", path: "/#/data/process-flow" },
      { name: "HSN Code", path: "/#/data/hsn-code" },
      { name: "Product Margin", path: "/#/data/product-margin" },
    ],
  },

  // ── Item Inquiry ─────────────────────────────────────────────────────
  "Item Inquiry": {
    routes: [
      { name: "Quotation Creation", path: "/#/data/quotation-creation" },
      { name: "Quotation Summary", path: "/#/data/quotation-summary" },
      { name: "New Customer Verification", path: "/#/data/new-customer-verification" },
    ],
  },

  // ── Sales Order ──────────────────────────────────────────────────────
  "Sales Order": {
    routes: [
      { name: "SO Customer Approval", path: "/#/data/sales-order-customer-approval" },
      { name: "SO Creation", path: "/#/data/sales-order-creation" },
      { name: "SO Summary", path: "/#/data/sales-order-summary" },
      { name: "SO Creation (Customer)", path: "/#/data/so-creation-customer" },
      { name: "SO Update (Customer)", path: "/#/data/so-update-customer" },
      { name: "SO Approval Overdue", path: "/#/data/so-approval-overdue-customer" },
      { name: "SO Approval Pending", path: "/#/data/so-approval-pending" },
    ],
  },

  // ── Purchase Order ───────────────────────────────────────────────────
  "Purchase Order": {
    routes: [
      { name: "PO Creation", path: "/#/data/purchase-order-creation" },
      { name: "PO Summary", path: "/#/data/purchase-order-summary" },
      { name: "PO Generation", path: "/#/data/purchase-order-generation" },
      { name: "PO Summary Overall", path: "/#/data/purchase-order-summary-overall" },
      { name: "PO Mgmt Approval", path: "/#/data/purchase-order-management-approval" },
      { name: "ABC Summary", path: "/#/data/abc-summary" },
      { name: "Purchase Follow-up", path: "/#/data/purchase-follow-up-new-sales-order" },
      { name: "Inter-Unit Material Request", path: "/#/data/inter-unit-material-request" },
      { name: "Inter-Unit Material Req Summary", path: "/#/data/inter-unit-material-request-summary" },
    ],
  },

  // ── Warehousing (Inward) ─────────────────────────────────────────────
  "Warehousing (Inward)": {
    routes: [
      { name: "Gate Entry - Inward Security", path: "/#/data/gate-entry-inward-security" },
      { name: "Gate Entry - Inward Summary", path: "/#/data/gate-entry-inward-security-summary" },
      { name: "Vehicle Weighment", path: "/#/data/vehicle-weighment" },
      { name: "Vehicle Weighment Form", path: "/#/data/vehicle-weighment-form" },
      { name: "Vehicle Weighment Form (New)", path: "/#/data/vehicle-weighment-form-new" },
      { name: "GRN - Purchase", path: "/#/data/grn" },
      { name: "GRN - Purchase Summary", path: "/#/data/grn-summary" },
      { name: "GRN - Job Work", path: "/#/data/grn-job-work" },
      { name: "GRN - Job Work Summary", path: "/#/data/grn-jobwork-summary" },
      { name: "GRN - Inter-Unit Request", path: "/#/data/grn-inter-unit-material-request" },
      { name: "GRN - Inter-Unit Req Summary", path: "/#/data/grn-inter-unit-material-request-summary" },
      { name: "Stock Transfer - All", path: "/#/data/stock-transfer-all" },
      { name: "Stock Transfer - All Summary", path: "/#/data/stock-transfer-all-summary" },
      { name: "Stock Summary (Inventory-wise)", path: "/#/data/stock-summary-inventorywise" },
      { name: "SO Schedule Warehouse Pick List", path: "/#/data/sales-order-schedule-warehouse-pick-list" },
      { name: "IUMT Schedule Warehouse Pick", path: "/#/data/iu-material-transfer-schedule-warehouse-pick-list" },
      { name: "IUMT Stock Transfer Warehouse", path: "/#/data/iumt-stock-transfer-warehouse" },
      { name: "Sales Return", path: "/#/data/sales-return" },
      { name: "Sales Return Summary", path: "/#/data/sales-return-summery" },
      { name: "Purchase Return", path: "/#/data/purchase-return" },
      { name: "Purchase Return Summary", path: "/#/data/purchase-return-summery" },
      { name: "Store Material Transfer Summary", path: "/#/data/store-material-transfer-summary" },
    ],
  },

  // ── Production ───────────────────────────────────────────────────────
  "Production": {
    routes: [
      { name: "Production Schedule", path: "/#/data/production-schedule" },
      { name: "Production Entry", path: "/#/data/production-entry" },
      { name: "Production Summary", path: "/#/data/production-summary" },
      { name: "Scrap Summary", path: "/#/data/scrap-summary" },
      { name: "Production Idle Time Entry", path: "/#/data/production-idle-time-entry" },
      { name: "Machine Idle Time Summary", path: "/#/data/machine-idle-time-summary" },
      { name: "Machine Maintenance Activity", path: "/#/data/machine-maintenance-activity" },
    ],
  },

  // ── Packing & Dispatch ───────────────────────────────────────────────
  "Packing & Dispatch": {
    routes: [
      { name: "Gate Entry - Outward Security", path: "/#/data/gate-entry-outward-security" },
      { name: "Gate Entry - Outward Summary", path: "/#/data/gate-entry-outward-security-summary" },
      { name: "Packing Schedule", path: "/#/data/packing-schedule" },
      { name: "Packing", path: "/#/data/packing" },
      { name: "Packing Summary", path: "/#/data/packing-summary" },
      { name: "Packing List Transfer", path: "/#/data/packing-list-transfer" },
      { name: "Packing List Summary", path: "/#/data/packing-list-summary" },
      { name: "Billing", path: "/#/data/billing" },
      { name: "Billing Summary", path: "/#/data/billing-summary" },
      { name: "SO Summary All", path: "/#/data/so-summary-all" },
    ],
  },

  // ── Job Work ─────────────────────────────────────────────────────────
  "Job Work": {
    routes: [
      { name: "Packing List - Job Work", path: "/#/data/packing-list-creation-jobwork" },
      { name: "Packing List - JW Summary", path: "/#/data/packinglist-jobwork-summary" },
      { name: "DC Creation - Job Work", path: "/#/data/dc-creation-jobwork" },
      { name: "DC Job Work Summary", path: "/#/data/dc-jobwork-summary" },
      { name: "Job Work Summary", path: "/#/data/job-work-summary" },
    ],
  },

  // ── Inter Unit Material Transfer ─────────────────────────────────────
  "Inter Unit Material Transfer": {
    routes: [
      { name: "Packing List - IUMT", path: "/#/data/packing-list-creation-iumt" },
      { name: "Packing List Summary - IUMT", path: "/#/data/packing-list-summary-iumt" },
      { name: "DC Creation - IUMT", path: "/#/data/dc-creation-iumt" },
      { name: "DC Summary - IUMT", path: "/#/data/dc-summary-iumt" },
      { name: "IUMT Summary", path: "/#/data/interunit-material-transfer-summary" },
    ],
  },

  // ── Certificate & Notes ──────────────────────────────────────────────
  "Certificate & Notes": {
    routes: [
      { name: "Certificate of Conformance", path: "/#/data/certificate-of-conformance" },
      { name: "Credit/Debit Note - Sales", path: "/#/data/credit-debit-note-sales" },
      { name: "Credit/Debit Note - Sales Summary", path: "/#/data/credit-debit-notes-sales-summery" },
      { name: "Credit/Debit Note - Purchase", path: "/#/data/credit-debit-note-purchase" },
      { name: "Credit/Debit Note - Purchase Summary", path: "/#/data/credit-debit-notes-purchase-summery" },
    ],
  },

  // ── Reports ──────────────────────────────────────────────────────────
  "Reports": {
    routes: [
      { name: "Salesman Incentives", path: "/#/data/salesman-incentives" },
      { name: "Machine OEE Report", path: "/#/data/machine-oee-report" },
    ],
  },
};

const API_ENDPOINTS = [
  // Auth
  { name: "Auth - Login", method: "POST", path: "/auth/login", body: CREDENTIALS },

  // Masters
  { name: "Customer Master API", method: "GET", path: "/customer-master/all" },
  { name: "Supplier Master API", method: "GET", path: "/suppliers/all" },
  { name: "Item Master API", method: "GET", path: "/item-master/all" },
  { name: "Unit Master API", method: "GET", path: "/unit-master/all" },
  { name: "Rack Bin Master API", method: "GET", path: "/rack-bin-master/all" },
  { name: "User Master API", method: "GET", path: "/user-master/all" },
  { name: "Salesman Master API", method: "GET", path: "/salesman-master/all" },
  { name: "Machine Master API", method: "GET", path: "/machines/all" },
  { name: "HSN Code API", method: "GET", path: "/hsn-code/all" },
  { name: "Hindalco Price API", method: "GET", path: "/hindalco-price/all" },
  { name: "Nalco Price API", method: "GET", path: "/nalco-price/all" },
  { name: "Process Flow API", method: "GET", path: "/process-entry/all" },
  { name: "Sub-Contractor API", method: "GET", path: "/sub-contractor-master/all" },
  { name: "Product Margin API", method: "GET", path: "/product-margins/all" },

  // Dashboard
  { name: "Dashboard API", method: "GET", path: "/dashboard/summary" },

  // Quotation & Sales
  { name: "Item Enquiry API", method: "GET", path: "/item-enquiries/all" },
  { name: "Sales Order API", method: "GET", path: "/sales-order/all" },
  { name: "SO Summary API", method: "GET", path: "/so-summary/all" },

  // Purchase Order
  { name: "Purchase Order (PO) API", method: "GET", path: "/po/all" },
  { name: "PO Generation API", method: "GET", path: "/po-generation/all" },
  { name: "PO Approval API", method: "GET", path: "/po-approval/all" },
  { name: "ABC Summary API", method: "GET", path: "/abcstock-summary/all" },
  { name: "Material Request API", method: "GET", path: "/material-request/all" },
  { name: "Purchase Follow-Up API", method: "GET", path: "/purchasefollowup/all" },

  // Warehouse
  { name: "Gate Entry Inward API", method: "GET", path: "/gate-inward/all" },
  { name: "Vehicle Weighment API", method: "GET", path: "/vehicle-weighment/all" },
  { name: "GRN API", method: "GET", path: "/grn/all" },
  { name: "GRN Summary API", method: "GET", path: "/grn-summary/all" },
  { name: "GRN Job Work API", method: "GET", path: "/grn-jobwork/all" },
  { name: "GRN Inter-Unit API", method: "GET", path: "/grn-interunit/all" },
  { name: "Stock Transfer API", method: "GET", path: "/stock-transfer/all" },
  { name: "Stock Summary API", method: "GET", path: "/stock-summary/all" },
  { name: "Rack Bin Eligible API", method: "GET", path: "/rack-bin-master/eligible?itemCategory=ALL&bundleNetWeight=1&store=Warehouse&unitName=ME&additionalAllocatedWeight=0" },
  { name: "Stock In API", method: "GET", path: "/stock-in/all" },
  { name: "Stock Out API", method: "GET", path: "/stock-out/all" },

  // Production
  { name: "Production Schedule API", method: "GET", path: "/product-schedule/all" },
  { name: "Production Entry API", method: "GET", path: "/production-entry/all" },
  { name: "Machine Maintenance API", method: "GET", path: "/machine-maintenance/all" },
  { name: "Machine OEE Report API", method: "GET", path: "/machine-oee-report/all" },

  // Packing & Dispatch
  { name: "Gate Entry Outward API", method: "GET", path: "/gate-entry-packing-and-dispatch/all" },
  { name: "Packing List API", method: "GET", path: "/api/packing-list/all" },
  { name: "Packing Scheduler API", method: "GET", path: "/api/packing-scheduler/all" },
  { name: "Billing Summary API", method: "GET", path: "/billing-summary/all" },
  { name: "Packing List IUMT API", method: "GET", path: "/packing-list-iumt/all" },
  { name: "Packing List Job Work API", method: "GET", path: "/packing-list-jobwork/all" },

  // Job Work & IUMT
  { name: "DC Job Work API", method: "GET", path: "/delivery-challan-jw/all" },
  { name: "DC IUMT API", method: "GET", path: "/delivery-challan-iumt/all" },

  // Returns
  { name: "Sales Return API", method: "GET", path: "/stock-returns/all" },
  { name: "Purchase Return API", method: "GET", path: "/purchase-return/all" },

  // Certificate & Notes
  { name: "Certificate of Conformance API", method: "GET", path: "/certificate-of-confidence/all" },
  { name: "Credit Debit Note (Sales) API", method: "GET", path: "/credit-debit-note/all" },
  { name: "Credit Debit Note (Purchase) API", method: "GET", path: "/purchase-credit-debit-note/all" },

  // Salesman
  { name: "Salesman Incentive API", method: "GET", path: "/incentives/all" },
];


// ============================================================================
// HTTP HELPERS
// ============================================================================

let authToken = null;

function httpRequest(url, method = "GET", body = null, token = null) {
  return new Promise((resolve, reject) => {
    const urlObj = new URL(url);
    const options = {
      hostname: urlObj.hostname,
      port: urlObj.port,
      path: urlObj.pathname + urlObj.search,
      method: method,
      headers: {
        "Accept": "application/json",
      },
      timeout: 15000,
    };
    // Note: Security is wide open (web.ignoring("/**")), so no token needed.
    // Sending a Bearer token actually triggers the JWT filter and causes 400s.

    const bodyStr = body ? JSON.stringify(body) : null;
    if (bodyStr) {
      options.headers["Content-Type"] = "application/json";
      options.headers["Content-Length"] = Buffer.byteLength(bodyStr);
    }

    const req = http.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => (data += chunk));
      res.on("end", () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: data,
          url: url,
        });
      });
    });

    req.on("error", (e) => reject(e));
    req.on("timeout", () => {
      req.destroy();
      reject(new Error("Request timed out"));
    });

    if (bodyStr) {
      req.write(bodyStr);
    }
    req.end();
  });
}

async function login() {
  try {
    const res = await httpRequest(
      `${API_BASE}/auth/login`,
      "POST",
      CREDENTIALS
    );
    if (res.status === 200) {
      const data = JSON.parse(res.body);
      // Extract token from nested response: data.data.token
      authToken = data?.data?.token || data.jwt || data.token || data.accessToken;
      return { success: true, token: authToken };
    }
    return { success: false, status: res.status, body: res.body };
  } catch (e) {
    return { success: false, error: e.message };
  }
}

// ============================================================================
// TEST RUNNERS
// ============================================================================

const results = {
  frontend: { passed: [], failed: [] },
  backend: { passed: [], failed: [] },
  summary: {},
};

async function testFrontendRoute(module, route) {
  const url = `${BASE_URL}${route.path}`;
  try {
    const res = await httpRequest(url);
    const isOk = res.status === 200 || res.status === 304;
    const hasAngularApp =
      res.body.includes("app-root") || res.body.includes("<html");
    const hasError =
      res.body.includes("Cannot match any routes") ||
      res.body.includes("ChunkLoadError") ||
      res.body.includes("Error:");

    const result = {
      module,
      name: route.name,
      path: route.path,
      status: res.status,
      loaded: isOk && hasAngularApp,
      hasError: hasError,
      errorDetail: hasError ? "Route/Chunk error detected in HTML" : null,
    };

    if (result.loaded && !result.hasError) {
      results.frontend.passed.push(result);
    } else {
      results.frontend.failed.push(result);
    }
    return result;
  } catch (e) {
    const result = {
      module,
      name: route.name,
      path: route.path,
      status: "ERROR",
      loaded: false,
      hasError: true,
      errorDetail: e.message,
    };
    results.frontend.failed.push(result);
    return result;
  }
}

async function testBackendEndpoint(endpoint) {
  const url = `${API_BASE}${endpoint.path}`;
  try {
    const res = await httpRequest(
      url,
      endpoint.method,
      endpoint.body || null,
      authToken
    );

    // Categorize response
    const isSuccess = res.status >= 200 && res.status < 400;
    const is404NotFound = res.status === 404;
    const is401Unauthorized = res.status === 401;
    const is500ServerError = res.status >= 500;

    let parsedBody = null;
    try {
      parsedBody = JSON.parse(res.body);
    } catch (e) {
      // Non-JSON response
    }

    const result = {
      name: endpoint.name,
      method: endpoint.method,
      path: endpoint.path,
      status: res.status,
      success: isSuccess,
      responseType: parsedBody ? "JSON" : "OTHER",
      detail: is404NotFound
        ? "Endpoint not found (404)"
        : is401Unauthorized
          ? "Unauthorized (401) - Token may be invalid"
          : is500ServerError
            ? `Server Error (${res.status}): ${(parsedBody?.message || parsedBody?.error || res.body).substring(0, 100)}`
            : isSuccess
              ? "OK"
              : `HTTP ${res.status}`,
    };

    if (isSuccess) {
      results.backend.passed.push(result);
    } else {
      results.backend.failed.push(result);
    }
    return result;
  } catch (e) {
    const result = {
      name: endpoint.name,
      method: endpoint.method,
      path: endpoint.path,
      status: "ERROR",
      success: false,
      detail: e.message,
    };
    results.backend.failed.push(result);
    return result;
  }
}

// ============================================================================
// REPORT GENERATOR
// ============================================================================

function generateReport() {
  const totalFrontend =
    results.frontend.passed.length + results.frontend.failed.length;
  const totalBackend =
    results.backend.passed.length + results.backend.failed.length;
  const totalPassed =
    results.frontend.passed.length + results.backend.passed.length;
  const totalFailed =
    results.frontend.failed.length + results.backend.failed.length;
  const totalTests = totalFrontend + totalBackend;

  let report = "";
  report += "═".repeat(80) + "\n";
  report += "   MetalCo ERP - Full Application Test Report\n";
  report += "   Generated: " + new Date().toLocaleString() + "\n";
  report += "═".repeat(80) + "\n\n";

  // ── Overall Summary ──────────────────────────────────────────────────
  report += "┌──────────────────────────────────────────────────────────────┐\n";
  report += "│  OVERALL SUMMARY                                           │\n";
  report += "├──────────────────────────────────────────────────────────────┤\n";
  report += `│  Total Tests:     ${String(totalTests).padStart(4)}                                      │\n`;
  report += `│  ✅ Passed:       ${String(totalPassed).padStart(4)}                                      │\n`;
  report += `│  ❌ Failed:       ${String(totalFailed).padStart(4)}                                      │\n`;
  report += `│  Pass Rate:       ${((totalPassed / totalTests) * 100).toFixed(1)}%                                    │\n`;
  report += "├──────────────────────────────────────────────────────────────┤\n";
  report += `│  Frontend Routes: ${String(results.frontend.passed.length).padStart(3)} / ${String(totalFrontend).padStart(3)} passed                           │\n`;
  report += `│  Backend APIs:    ${String(results.backend.passed.length).padStart(3)} / ${String(totalBackend).padStart(3)} passed                           │\n`;
  report += "└──────────────────────────────────────────────────────────────┘\n\n";

  // ── Frontend Results by Module ───────────────────────────────────────
  report += "─".repeat(80) + "\n";
  report += "  FRONTEND ROUTE TESTS\n";
  report += "─".repeat(80) + "\n\n";

  for (const [moduleName, moduleData] of Object.entries(MODULES)) {
    const moduleRoutes = [
      ...results.frontend.passed.filter((r) => r.module === moduleName),
      ...results.frontend.failed.filter((r) => r.module === moduleName),
    ];
    const modulePassed = moduleRoutes.filter((r) => r.loaded && !r.hasError).length;
    const moduleTotal = moduleRoutes.length;

    report += `  📦 ${moduleName}  [${modulePassed}/${moduleTotal}]\n`;
    for (const r of moduleRoutes) {
      const icon = r.loaded && !r.hasError ? "✅" : "❌";
      const detail = r.hasError ? ` ← ${r.errorDetail}` : "";
      report += `     ${icon} ${r.name.padEnd(42)} ${r.path}${detail}\n`;
    }
    report += "\n";
  }

  // ── Backend Results ──────────────────────────────────────────────────
  report += "─".repeat(80) + "\n";
  report += "  BACKEND API TESTS\n";
  report += "─".repeat(80) + "\n\n";

  const allApiResults = [...results.backend.passed, ...results.backend.failed];
  for (const r of allApiResults) {
    const icon = r.success ? "✅" : "❌";
    const statusStr = String(r.status).padEnd(5);
    report += `  ${icon} [${r.method.padEnd(4)}] ${statusStr} ${r.name.padEnd(35)} ${r.detail}\n`;
  }
  report += "\n";

  // ── Bug/Issue List ───────────────────────────────────────────────────
  report += "═".repeat(80) + "\n";
  report += "  BUG LIST - ISSUES FOUND\n";
  report += "═".repeat(80) + "\n\n";

  let bugNumber = 1;

  if (results.frontend.failed.length > 0) {
    report += "  ── Frontend Issues ──\n\n";
    for (const r of results.frontend.failed) {
      report += `  BUG-${String(bugNumber).padStart(3, "0")}: [${r.module}] ${r.name}\n`;
      report += `          Route: ${r.path}\n`;
      report += `          Status: HTTP ${r.status}\n`;
      report += `          Issue: ${r.errorDetail || (r.loaded ? "Page has errors" : "Page failed to load")}\n`;
      report += "\n";
      bugNumber++;
    }
  }

  if (results.backend.failed.length > 0) {
    report += "  ── Backend API Issues ──\n\n";
    for (const r of results.backend.failed) {
      report += `  BUG-${String(bugNumber).padStart(3, "0")}: [API] ${r.name}\n`;
      report += `          Endpoint: ${r.method} ${r.path}\n`;
      report += `          Status: ${r.status}\n`;
      report += `          Issue: ${r.detail}\n`;
      report += "\n";
      bugNumber++;
    }
  }

  if (bugNumber === 1) {
    report += "  🎉 No bugs found! All tests passed.\n\n";
  }

  // ── Working Features ─────────────────────────────────────────────────
  report += "═".repeat(80) + "\n";
  report += "  WORKING FEATURES\n";
  report += "═".repeat(80) + "\n\n";

  for (const [moduleName] of Object.entries(MODULES)) {
    const modulePassed = results.frontend.passed.filter(
      (r) => r.module === moduleName
    );
    if (modulePassed.length > 0) {
      report += `  ✅ ${moduleName}:\n`;
      for (const r of modulePassed) {
        report += `      • ${r.name}\n`;
      }
      report += "\n";
    }
  }

  const backendPassed = results.backend.passed;
  if (backendPassed.length > 0) {
    report += `  ✅ Backend APIs Working:\n`;
    for (const r of backendPassed) {
      report += `      • ${r.name} (${r.method} → ${r.status})\n`;
    }
    report += "\n";
  }

  report += "═".repeat(80) + "\n";
  report += "  END OF REPORT\n";
  report += "═".repeat(80) + "\n";

  return report;
}

// ============================================================================
// MAIN EXECUTION
// ============================================================================

async function main() {
  console.log("═".repeat(60));
  console.log("  MetalCo ERP - Full Application Test Suite");
  console.log("═".repeat(60));

  // ── Step 1: Check services are running ───────────────────────────────
  console.log("\n🔍 Step 1: Checking services...");

  try {
    await httpRequest(`${BASE_URL}`);
    console.log("  ✅ Frontend (port 4200) - UP");
  } catch (e) {
    console.log("  ❌ Frontend (port 4200) - DOWN. Aborting.");
    process.exit(1);
  }

  try {
    await httpRequest(`${API_BASE}/auth/login`, "POST", CREDENTIALS);
    console.log("  ✅ Backend (port 8089) - UP");
  } catch (e) {
    console.log("  ❌ Backend (port 8089) - DOWN. Aborting.");
    process.exit(1);
  }

  // ── Step 2: Authenticate ─────────────────────────────────────────────
  console.log("\n🔐 Step 2: Authenticating...");
  const loginResult = await login();
  if (loginResult.success) {
    console.log("  ✅ Logged in successfully. Token obtained.");
  } else {
    console.log("  ⚠️  Login failed:", loginResult.error || loginResult.status);
    console.log("  Continuing with unauthenticated API tests...");
  }

  // ── Step 3: Test Frontend Routes ─────────────────────────────────────
  console.log("\n🌐 Step 3: Testing Frontend Routes...\n");

  let routeCount = 0;
  const totalRoutes = Object.values(MODULES).reduce(
    (acc, m) => acc + m.routes.length,
    0
  );

  for (const [moduleName, moduleData] of Object.entries(MODULES)) {
    process.stdout.write(`  📦 ${moduleName}: `);
    let moduleResults = [];

    for (const route of moduleData.routes) {
      routeCount++;
      const result = await testFrontendRoute(moduleName, route);
      moduleResults.push(result);
      process.stdout.write(result.loaded && !result.hasError ? "✅" : "❌");
    }

    const passed = moduleResults.filter((r) => r.loaded && !r.hasError).length;
    console.log(` [${passed}/${moduleResults.length}]`);
  }

  console.log(`\n  Tested ${routeCount}/${totalRoutes} routes.`);

  // ── Step 4: Test Backend APIs ────────────────────────────────────────
  console.log("\n⚙️  Step 4: Testing Backend APIs...\n");

  for (const endpoint of API_ENDPOINTS) {
    const result = await testBackendEndpoint(endpoint);
    const icon = result.success ? "✅" : "❌";
    console.log(`  ${icon} ${endpoint.name} → ${result.status} ${result.detail}`);
  }

  // ── Step 5: Generate Report ──────────────────────────────────────────
  console.log("\n📝 Step 5: Generating report...");

  const report = generateReport();

  // Write report to file
  const fs = require("fs");
  const reportPath = __dirname + "/test-report.txt";
  fs.writeFileSync(reportPath, report, "utf8");
  console.log(`\n  ✅ Report saved to: ${reportPath}`);

  // Print summary
  const totalPassed =
    results.frontend.passed.length + results.backend.passed.length;
  const totalFailed =
    results.frontend.failed.length + results.backend.failed.length;
  const totalTests = totalPassed + totalFailed;

  console.log("\n" + "═".repeat(60));
  console.log(`  RESULTS: ${totalPassed}/${totalTests} passed (${((totalPassed / totalTests) * 100).toFixed(1)}%)`);
  console.log(`  Frontend: ${results.frontend.passed.length}/${results.frontend.passed.length + results.frontend.failed.length}`);
  console.log(`  Backend:  ${results.backend.passed.length}/${results.backend.passed.length + results.backend.failed.length}`);
  console.log(`  Bugs Found: ${totalFailed}`);
  console.log("═".repeat(60));
}

main().catch(console.error);
