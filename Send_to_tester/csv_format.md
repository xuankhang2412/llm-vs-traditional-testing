# Standard CSV Format Specification

This document defines the standard CSV input/output format for the test suites across all four target applications.

## 1. Test Suite Quotas
- **Basic Applications** (BasicPackage, BasicPayroll): **20 test cases per suite**
- **Enhanced Applications** (EnhancedPackage, EnhancedPayroll): **35 test cases per suite**

## 2. General Formatting Rules
- All files must be UTF-8 encoded text files using comma (`,`) as the delimiter.
- The first line must contain the exact header specified below.
- Numeric floating-point values use standard decimal point (`.`).
- The final column `expected` must contain either:
  - The expected numeric calculation result rounded to 2 decimal places (e.g. `131.40`, `553.82`, `20.00`).
  - The literal word `ERROR` if the input vector violates any domain constraint, dependency, or business validation rule.

---

## 3. Application Fixed CSV Headers

### 3.1 Basic Package (`BasicPackage`)
```csv
weight,origin,destination,deliveryOption,signatureOption,deliveryLocationType,saturdayDelivery,holdAtLocation,fragile,dangerous,expected
```

### 3.2 Basic Payroll (`BasicPayroll`)
```csv
hourlySalary,regularHours,overtimeHours,holidayHours,medicalPlan,dentalPlan,visionPlan,unitedWay,pension,longTermDisability,shortTermDisability,parking,expected
```

### 3.3 Enhanced Package (`EnhancedPackage`)
```csv
weight,origin,destination,deliveryOption,packaging,signature,locationType,saturday,hold,fragile,dangerous,expected
```

### 3.4 Enhanced Payroll (`EnhancedPayroll`)
```csv
hourly,regularHours,overtimeHours,holidayHours,state,medical,dental,vision,unitedWay,pension,ltd,std,parking,expected
```
