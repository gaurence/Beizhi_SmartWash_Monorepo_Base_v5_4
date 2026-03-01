package com.beizhi.smartwash;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformGateIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void tenantIsolationBlocksCrossTenantCabinetAccess() throws Exception {
    mockMvc.perform(withHeaders(get("/assets/cabinets/CAB-A1"), "tenant-a", "asset.cabinet.read"))
      .andExpect(status().isOk());

    mockMvc.perform(withHeaders(get("/assets/cabinets/CAB-A1"), "tenant-b", "asset.cabinet.read"))
      .andExpect(status().isNotFound());
  }

  @Test
  void permissionChecksSupportAllowAndDeny() throws Exception {
    mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk());
  }

  @Test
  void riskPolicyApisEnforcePermissionIsolationIdempotencyAndAudit() throws Exception {
    mockMvc.perform(withHeaders(get("/risk/policies"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    JsonNode listNode = objectMapper.readTree(mockMvc.perform(withHeaders(
        get("/risk/policies"),
        "tenant-a",
        "risk.policy.read"
      ))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(listNode.at("/total").asLong()).isGreaterThan(0L);
    String policyId = listNode.at("/data/0/policyId").asText();
    assertThat(policyId).isNotBlank();

    String payload = """
      {
        "status": "DISABLED",
        "triggerScore": 88,
        "reviewWindowMinutes": 45
      }
      """;

    mockMvc.perform(withHeaders(put("/risk/policies/" + policyId), "tenant-b", "risk.policy.manage")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());

    MockHttpServletRequestBuilder updateRequest = withHeaders(
      put("/risk/policies/" + policyId),
      "tenant-a",
      "risk.policy.manage,audit.log.read"
    )
      .header("Idempotency-Key", "idem-risk-policy-update-iter19")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    JsonNode firstUpdate = objectMapper.readTree(mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode secondUpdate = objectMapper.readTree(mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());

    assertThat(firstUpdate.at("/data/policyId").asText()).isEqualTo(policyId);
    assertThat(firstUpdate.at("/data/status").asText()).isEqualTo("DISABLED");
    assertThat(firstUpdate.at("/data/triggerScore").asInt()).isEqualTo(88);
    assertThat(firstUpdate.at("/data/reviewWindowMinutes").asInt()).isEqualTo(45);
    assertThat(firstUpdate.at("/data/requestId").asText())
      .isEqualTo(secondUpdate.at("/data/requestId").asText());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("RISK_POLICY_UPDATE".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  @Test
  void openSlotIsIdempotentByIdempotencyKey() throws Exception {
    String payload = """
      {
        "slotNo": "S1",
        "nextState": "OCCUPIED",
        "action": "PICKUP"
      }
      """;

    MockHttpServletRequestBuilder request = withHeaders(
      post("/iot/cabinets/CAB-A1/commands/open-slot"),
      "tenant-a",
      "iot.open-slot.execute"
    )
      .header("Idempotency-Key", "idem-open-slot-1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    MvcResult first = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
    MvcResult second = mockMvc.perform(request).andExpect(status().isOk()).andReturn();

    JsonNode firstNode = objectMapper.readTree(first.getResponse().getContentAsString());
    JsonNode secondNode = objectMapper.readTree(second.getResponse().getContentAsString());

    assertThat(firstNode.at("/data/commandId").asText())
      .isEqualTo(secondNode.at("/data/commandId").asText());
    assertThat(firstNode.at("/data/requestId").asText())
      .isEqualTo(secondNode.at("/data/requestId").asText());
  }

  @Test
  void slotStateMachineRejectsIllegalTransition() throws Exception {
    String toLocked = """
      {
        "slotNo": "S2",
        "nextState": "LOCKED"
      }
      """;

    mockMvc.perform(withHeaders(post("/iot/cabinets/CAB-A1/commands/open-slot"), "tenant-a", "iot.open-slot.execute")
        .header("Idempotency-Key", "idem-open-slot-2")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toLocked))
      .andExpect(status().isOk());

    String invalidBackToOccupied = """
      {
        "slotNo": "S2",
        "nextState": "OCCUPIED"
      }
      """;

    mockMvc.perform(withHeaders(post("/iot/cabinets/CAB-A1/commands/open-slot"), "tenant-a", "iot.open-slot.execute")
        .header("Idempotency-Key", "idem-open-slot-3")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidBackToOccupied))
      .andExpect(status().isConflict());
  }

  @Test
  void highRiskActionWritesAppendOnlyAuditLog() throws Exception {
    String createBinding = """
      {
        "storeId": "STORE-X",
        "bindingType": "SHARED"
      }
      """;

    mockMvc.perform(withHeaders(post("/asset/cabinets/CAB-A1/store-bindings"), "tenant-a", "asset.binding.manage")
        .header("Idempotency-Key", "idem-binding-create-1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(createBinding))
      .andExpect(status().isOk());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    assertThat(logsNode.at("/total").asLong()).isGreaterThan(0L);

    boolean foundAction = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("CABINET_STORE_BINDING_CREATE".equals(item.path("action").asText())) {
        foundAction = true;
        break;
      }
    }
    assertThat(foundAction).isTrue();
  }

  @Test
  void appSmsLoginReturnsTenantBoundTokenAndMaskedPhone() throws Exception {
    String body = """
      {
        "mobile": "13800000000",
        "code": "123456"
      }
      """;

    MvcResult result = mockMvc.perform(post("/app/auth/sms/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(node.at("/data/token").asText()).startsWith("mock-jwt:tenant-a:");
    assertThat(node.at("/data/mobileMasked").asText()).isEqualTo("138****0000");

    String invalid = """
      {
        "mobile": "13800000000",
        "code": "654321"
      }
      """;

    mockMvc.perform(post("/app/auth/sms/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalid))
      .andExpect(status().isBadRequest());
  }

  @Test
  void appSiteReadUsesTokenTenantWhenHeaderTenantIsMissing() throws Exception {
    MvcResult tenantA = mockMvc.perform(get("/app/sites/nearby")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.site.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode tenantANode = objectMapper.readTree(tenantA.getResponse().getContentAsString());
    assertThat(tenantANode.toString()).contains("SITE-A");
    assertThat(tenantANode.toString()).doesNotContain("SITE-B");

    MvcResult tenantB = mockMvc.perform(get("/app/sites/nearby")
        .header("Authorization", "Bearer mock-jwt:tenant-b:customer-0001")
        .header("X-Permissions", "app.site.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode tenantBNode = objectMapper.readTree(tenantB.getResponse().getContentAsString());
    assertThat(tenantBNode.toString()).contains("SITE-B");
    assertThat(tenantBNode.toString()).doesNotContain("SITE-A");
  }

  @Test
  void appOrderCreateIsIdempotentAndTenantIsolated() throws Exception {
    String payload = """
      {
        "fulfillmentMode": "onsite",
        "servicePackageId": "STANDARD_CARE",
        "servicePackageName": "Standard Care",
        "addonIds": ["ADDON_INTERIOR"],
        "amountSubtotal": 129,
        "amountDiscount": 10,
        "amountTotal": 119
      }
      """;

    MockHttpServletRequestBuilder create = post("/app/orders")
      .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
      .header("X-Permissions", "app.order.create,app.order.read")
      .header("Idempotency-Key", "idem-app-order-iter08")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    JsonNode first = objectMapper.readTree(mockMvc.perform(create).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(create).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    String orderId = first.at("/data/orderId").asText();

    assertThat(orderId).isEqualTo(second.at("/data/orderId").asText());

    mockMvc.perform(get("/app/orders/" + orderId)
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.order.read"))
      .andExpect(status().isOk());

    mockMvc.perform(get("/app/orders/" + orderId)
        .header("Authorization", "Bearer mock-jwt:tenant-b:customer-0001")
        .header("X-Permissions", "app.order.read"))
      .andExpect(status().isNotFound());

    MvcResult timeline = mockMvc.perform(get("/app/orders/" + orderId + "/timeline")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.order.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode timelineNode = objectMapper.readTree(timeline.getResponse().getContentAsString());
    assertThat(timelineNode.toString()).contains("ORDER_CREATED");
  }

  @Test
  void appDelegateRetrieveCodeEnforcesPermissionIsolationIdempotencyAndAudit() throws Exception {
    String payload = """
      {
        "cabinetId": "CAB-A1",
        "slotId": "S1",
        "delegateMobile": "13800138000",
        "ttlSeconds": 180
      }
      """;

    mockMvc.perform(post("/app/orders/ORD-TENANT-A-0001/delegate-retrieve-code")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isForbidden());

    MockHttpServletRequestBuilder issueRequest = post("/app/orders/ORD-TENANT-A-0001/delegate-retrieve-code")
      .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
      .header("X-Permissions", "app.order.delegate")
      .header("Idempotency-Key", "idem-app-delegate-code-iter13")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    JsonNode first = objectMapper.readTree(mockMvc.perform(issueRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(issueRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(first.at("/data/delegateRetrieveCode").asText())
      .isEqualTo(second.at("/data/delegateRetrieveCode").asText());
    assertThat(first.at("/data/delegateRetrieveToken").asText())
      .isEqualTo(second.at("/data/delegateRetrieveToken").asText());
    assertThat(first.at("/data/requestId").asText())
      .isEqualTo(second.at("/data/requestId").asText());
    assertThat(first.at("/data/delegateMobileMasked").asText()).isEqualTo("138****8000");

    mockMvc.perform(post("/app/orders/ORD-TENANT-A-0001/delegate-retrieve-code")
        .header("Authorization", "Bearer mock-jwt:tenant-b:customer-0001")
        .header("X-Permissions", "app.order.delegate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());

    MvcResult timeline = mockMvc.perform(get("/app/orders/ORD-TENANT-A-0001/timeline")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.order.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode timelineNode = objectMapper.readTree(timeline.getResponse().getContentAsString());
    assertThat(timelineNode.toString()).contains("ORDER_DELEGATE_RETRIEVE_CODE_ISSUED");

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());

    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("APP_ORDER_DELEGATE_RETRIEVE_CODE_ISSUE".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  @Test
  void appOnsiteCabinetScanDepositRetrieveEnforcePermissionIsolationIdempotencyAndAudit() throws Exception {
    String scanPayload = """
      {
        "keyword": "CAB-A"
      }
      """;

    mockMvc.perform(post("/app/cabinets/scan")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .contentType(MediaType.APPLICATION_JSON)
        .content(scanPayload))
      .andExpect(status().isForbidden());

    MvcResult scanResult = mockMvc.perform(post("/app/cabinets/scan")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.cabinet.scan")
        .contentType(MediaType.APPLICATION_JSON)
        .content(scanPayload))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode scanNode = objectMapper.readTree(scanResult.getResponse().getContentAsString());
    assertThat(scanNode.toString()).contains("CAB-A1");
    assertThat(scanNode.toString()).doesNotContain("CAB-B1");

    String createPayload = """
      {
        "fulfillmentMode": "ONSITE",
        "servicePackageId": "STANDARD_CARE",
        "servicePackageName": "Standard Care"
      }
      """;
    JsonNode createdOrder = objectMapper.readTree(mockMvc.perform(post("/app/orders")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.order.create")
        .header("Idempotency-Key", "idem-app-order-create-iter15")
        .contentType(MediaType.APPLICATION_JSON)
        .content(createPayload))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    String orderId = createdOrder.at("/data/orderId").asText();
    assertThat(orderId).isNotBlank();

    String depositPayload = """
      {
        "cabinetId": "CAB-A1",
        "slotNo": "S1"
      }
      """;
    MockHttpServletRequestBuilder depositRequest = post("/app/orders/" + orderId + "/deposit-key")
      .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
      .header("X-Permissions", "app.order.deposit-key")
      .header("Idempotency-Key", "idem-app-order-deposit-key-iter15")
      .contentType(MediaType.APPLICATION_JSON)
      .content(depositPayload);

    JsonNode depositFirst = objectMapper.readTree(mockMvc.perform(depositRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode depositSecond = objectMapper.readTree(mockMvc.perform(depositRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(depositFirst.at("/data/status").asText()).isEqualTo("KEY_DEPOSITED");
    assertThat(depositFirst.at("/data/requestId").asText()).isEqualTo(depositSecond.at("/data/requestId").asText());

    mockMvc.perform(post("/app/orders/" + orderId + "/deposit-key")
        .header("Authorization", "Bearer mock-jwt:tenant-b:customer-0001")
        .header("X-Permissions", "app.order.deposit-key")
        .contentType(MediaType.APPLICATION_JSON)
        .content(depositPayload))
      .andExpect(status().isNotFound());

    MockHttpServletRequestBuilder retrieveRequest = post("/app/orders/" + orderId + "/retrieve-key")
      .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
      .header("X-Permissions", "app.order.retrieve-key")
      .header("Idempotency-Key", "idem-app-order-retrieve-key-iter15")
      .contentType(MediaType.APPLICATION_JSON)
      .content(depositPayload);

    JsonNode retrieveFirst = objectMapper.readTree(mockMvc.perform(retrieveRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode retrieveSecond = objectMapper.readTree(mockMvc.perform(retrieveRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(retrieveFirst.at("/data/status").asText()).isEqualTo("KEY_RETRIEVED");
    assertThat(retrieveFirst.at("/data/requestId").asText()).isEqualTo(retrieveSecond.at("/data/requestId").asText());

    mockMvc.perform(post("/app/orders/" + orderId + "/retrieve-key")
        .header("Authorization", "Bearer mock-jwt:tenant-b:customer-0001")
        .header("X-Permissions", "app.order.retrieve-key")
        .contentType(MediaType.APPLICATION_JSON)
        .content(depositPayload))
      .andExpect(status().isNotFound());

    MvcResult timeline = mockMvc.perform(get("/app/orders/" + orderId + "/timeline")
        .header("Authorization", "Bearer mock-jwt:tenant-a:customer-0000")
        .header("X-Permissions", "app.order.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode timelineNode = objectMapper.readTree(timeline.getResponse().getContentAsString());
    assertThat(timelineNode.toString()).contains("ORDER_KEY_DEPOSITED");
    assertThat(timelineNode.toString()).contains("ORDER_KEY_RETRIEVED");

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean depositedAudit = false;
    boolean retrievedAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("APP_ORDER_KEY_DEPOSIT".equals(item.path("action").asText())) {
        depositedAudit = true;
      }
      if ("APP_ORDER_KEY_RETRIEVE".equals(item.path("action").asText())) {
        retrievedAudit = true;
      }
    }
    assertThat(depositedAudit).isTrue();
    assertThat(retrievedAudit).isTrue();
  }

  @Test
  void appTechTaskApisEnforceTenantIsolationPermissionIdempotencyAndAudit() throws Exception {
    mockMvc.perform(get("/app/tech/tasks")
        .header("Authorization", "Bearer mock-jwt:tenant-a:tech01"))
      .andExpect(status().isForbidden());

    MvcResult listResult = mockMvc.perform(get("/app/tech/tasks")
        .header("Authorization", "Bearer mock-jwt:tenant-a:tech01")
        .header("X-Permissions", "app.task.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode listNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
    assertThat(listNode.toString()).contains("TASK-TENANT-A-0001");
    assertThat(listNode.toString()).doesNotContain("TASK-TENANT-B-0001");

    MockHttpServletRequestBuilder acceptRequest = post("/app/tech/tasks/TASK-TENANT-A-0001/accept")
      .header("Authorization", "Bearer mock-jwt:tenant-a:tech01")
      .header("X-Permissions", "app.task.accept")
      .header("Idempotency-Key", "idem-app-tech-task-accept-iter11")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}");

    JsonNode acceptFirst = objectMapper.readTree(mockMvc.perform(acceptRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode acceptSecond = objectMapper.readTree(mockMvc.perform(acceptRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(acceptFirst.at("/data/status").asText()).isEqualTo("ACCEPTED");
    assertThat(acceptFirst.at("/data/requestId").asText()).isEqualTo(acceptSecond.at("/data/requestId").asText());

    mockMvc.perform(post("/app/tech/tasks/TASK-TENANT-A-0001/complete")
        .header("Authorization", "Bearer mock-jwt:tenant-a:tech02")
        .header("X-Permissions", "app.task.complete")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isForbidden());

    MockHttpServletRequestBuilder completeRequest = post("/app/tech/tasks/TASK-TENANT-A-0001/complete")
      .header("Authorization", "Bearer mock-jwt:tenant-a:tech01")
      .header("X-Permissions", "app.task.complete")
      .header("Idempotency-Key", "idem-app-tech-task-complete-iter11")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}");

    JsonNode completeFirst = objectMapper.readTree(mockMvc.perform(completeRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode completeSecond = objectMapper.readTree(mockMvc.perform(completeRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(completeFirst.at("/data/status").asText()).isEqualTo("COMPLETED");
    assertThat(completeFirst.at("/data/requestId").asText()).isEqualTo(completeSecond.at("/data/requestId").asText());

    mockMvc.perform(post("/app/tech/tasks/TASK-TENANT-A-0001/accept")
        .header("Authorization", "Bearer mock-jwt:tenant-b:tech01")
        .header("X-Permissions", "app.task.accept")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isNotFound());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());

    boolean acceptedAudit = false;
    boolean completedAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("APP_TECH_TASK_ACCEPT".equals(item.path("action").asText())) {
        acceptedAudit = true;
      }
      if ("APP_TECH_TASK_COMPLETE".equals(item.path("action").asText())) {
        completedAudit = true;
      }
    }
    assertThat(acceptedAudit).isTrue();
    assertThat(completedAudit).isTrue();
  }

  @Test
  void orderApisEnforceTenantIsolationPermissionIdempotencyAndAudit() throws Exception {
    mockMvc.perform(withHeaders(get("/orders"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    MvcResult listResult = mockMvc.perform(withHeaders(get("/orders"), "tenant-a", "order.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode listNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
    assertThat(listNode.toString()).contains("ORD-TENANT-A-0001");
    assertThat(listNode.toString()).doesNotContain("ORD-TENANT-B-0001");

    String createPayload = """
      {
        "servicePackageId": "PREMIUM_CARE",
        "servicePackageName": "Premium Care",
        "amountSubtotal": 199,
        "amountDiscount": 20,
        "amountTotal": 179
      }
      """;

    MockHttpServletRequestBuilder createRequest = withHeaders(post("/orders"), "tenant-a", "order.manage")
      .header("Idempotency-Key", "idem-order-create-iter12")
      .contentType(MediaType.APPLICATION_JSON)
      .content(createPayload);

    JsonNode createFirst = objectMapper.readTree(mockMvc.perform(createRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode createSecond = objectMapper.readTree(mockMvc.perform(createRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());

    String orderId = createFirst.at("/data/orderId").asText();
    assertThat(orderId).isEqualTo(createSecond.at("/data/orderId").asText());

    mockMvc.perform(withHeaders(get("/orders/" + orderId), "tenant-a", "order.read"))
      .andExpect(status().isOk());
    mockMvc.perform(withHeaders(get("/orders/" + orderId), "tenant-b", "order.read"))
      .andExpect(status().isNotFound());

    String dispatchPayload = """
      {
        "assigneeUserId": "tech01"
      }
      """;
    MockHttpServletRequestBuilder dispatchRequest = withHeaders(
      post("/orders/" + orderId + "/dispatch"),
      "tenant-a",
      "order.manage"
    )
      .header("Idempotency-Key", "idem-order-dispatch-iter12")
      .contentType(MediaType.APPLICATION_JSON)
      .content(dispatchPayload);

    JsonNode dispatchFirst = objectMapper.readTree(mockMvc.perform(dispatchRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode dispatchSecond = objectMapper.readTree(mockMvc.perform(dispatchRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(dispatchFirst.at("/data/status").asText()).isEqualTo("DISPATCHED");
    assertThat(dispatchFirst.at("/data/requestId").asText())
      .isEqualTo(dispatchSecond.at("/data/requestId").asText());

    String cancelPayload = """
      {
        "reason": "STORE_CLOSED"
      }
      """;
    MockHttpServletRequestBuilder cancelRequest = withHeaders(
      post("/orders/" + orderId + "/cancel"),
      "tenant-a",
      "order.manage"
    )
      .header("Idempotency-Key", "idem-order-cancel-iter12")
      .contentType(MediaType.APPLICATION_JSON)
      .content(cancelPayload);

    JsonNode cancelFirst = objectMapper.readTree(mockMvc.perform(cancelRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode cancelSecond = objectMapper.readTree(mockMvc.perform(cancelRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(cancelFirst.at("/data/status").asText()).isEqualTo("CANCELLED");
    assertThat(cancelFirst.at("/data/requestId").asText())
      .isEqualTo(cancelSecond.at("/data/requestId").asText());

    mockMvc.perform(withHeaders(post("/orders/" + orderId + "/dispatch"), "tenant-a", "order.manage")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isBadRequest());

    MvcResult timelineResult = mockMvc.perform(withHeaders(get("/orders/" + orderId + "/timeline"), "tenant-a", "order.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode timelineNode = objectMapper.readTree(timelineResult.getResponse().getContentAsString());
    assertThat(timelineNode.toString()).contains("ORDER_DISPATCHED");
    assertThat(timelineNode.toString()).contains("ORDER_CANCELLED");

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());

    boolean dispatchAudit = false;
    boolean cancelAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("ORDER_DISPATCH".equals(item.path("action").asText())) {
        dispatchAudit = true;
      }
      if ("ORDER_CANCEL".equals(item.path("action").asText())) {
        cancelAudit = true;
      }
    }
    assertThat(dispatchAudit).isTrue();
    assertThat(cancelAudit).isTrue();
  }

  @Test
  void orderEvidenceApiEnforcesTenantIsolationAndPermission() throws Exception {
    mockMvc.perform(withHeaders(get("/orders/ORD-TENANT-A-0001/evidence"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    MvcResult allowResult = mockMvc.perform(withHeaders(
        get("/orders/ORD-TENANT-A-0001/evidence"),
        "tenant-a",
        "order.read"
      ))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode allowNode = objectMapper.readTree(allowResult.getResponse().getContentAsString());
    assertThat(allowNode.toString()).contains("EVD-ORD-TENANT-A-0001-001");
    assertThat(allowNode.toString()).doesNotContain("EVD-ORD-TENANT-B-0001-001");

    mockMvc.perform(withHeaders(
        get("/orders/ORD-TENANT-A-0001/evidence"),
        "tenant-b",
        "order.read"
      ))
      .andExpect(status().isNotFound());
  }

  @Test
  void deviceInvestorReadChecksPermissionAndTenantIsolation() throws Exception {
    mockMvc.perform(withHeaders(get("/asset/device-investors"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    mockMvc.perform(withHeaders(get("/asset/device-investors/INV-A1"), "tenant-a", "asset.investor.read"))
      .andExpect(status().isOk());

    mockMvc.perform(withHeaders(get("/asset/device-investors/INV-A1"), "tenant-b", "asset.investor.read"))
      .andExpect(status().isNotFound());

    MvcResult cabinets = mockMvc.perform(withHeaders(
        get("/asset/device-investors/INV-A1/cabinets"),
        "tenant-a",
        "asset.investor.read"
      ))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode node = objectMapper.readTree(cabinets.getResponse().getContentAsString());
    assertThat(node.toString()).contains("CAB-A1");
  }

  @Test
  void tenantOrgAndUserApisEnforceTenantIsolation() throws Exception {
    MvcResult users = mockMvc.perform(withHeaders(get("/users"), "tenant-a", "iam.user.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode usersNode = objectMapper.readTree(users.getResponse().getContentAsString());
    String userId = usersNode.path("data").get(0).path("id").asText();
    assertThat(userId).isNotBlank();

    mockMvc.perform(withHeaders(get("/users/" + userId), "tenant-a", "iam.user.read"))
      .andExpect(status().isOk());

    mockMvc.perform(withHeaders(get("/users/" + userId), "tenant-b", "iam.user.read"))
      .andExpect(status().isNotFound());
  }

  @Test
  void tenantCreateAndAuditExportAreIdempotent() throws Exception {
    String tenantCreateBody = """
      {
        "tenantCode": "ITER02",
        "tenantName": "ITER02租户"
      }
      """;
    MockHttpServletRequestBuilder createTenant = withHeaders(post("/tenants"), "tenant-a", "platform.tenant.create")
      .header("Idempotency-Key", "idem-tenant-create-iter02")
      .contentType(MediaType.APPLICATION_JSON)
      .content(tenantCreateBody);

    JsonNode firstTenant = objectMapper.readTree(mockMvc.perform(createTenant).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode secondTenant = objectMapper.readTree(mockMvc.perform(createTenant).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertThat(firstTenant.at("/data/id").asText()).isEqualTo(secondTenant.at("/data/id").asText());

    String auditExportBody = """
      {
        "from": "2026-02-01T00:00:00Z",
        "to": "2026-02-28T00:00:00Z",
        "action": "IAM_ROLE_PERMISSION_BIND"
      }
      """;
    MockHttpServletRequestBuilder export = withHeaders(post("/audit/exports"), "tenant-a", "audit.export")
      .header("Idempotency-Key", "idem-audit-export-iter02")
      .contentType(MediaType.APPLICATION_JSON)
      .content(auditExportBody);

    JsonNode firstExport = objectMapper.readTree(mockMvc.perform(export).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode secondExport = objectMapper.readTree(mockMvc.perform(export).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertThat(firstExport.at("/data/id").asText()).isEqualTo(secondExport.at("/data/id").asText());
    assertThat(firstExport.at("/data/downloadPath").asText()).contains("/exports/audit/");
  }

  @Test
  void rolePermissionBindingRequiresPermissionAndWritesAudit() throws Exception {
    mockMvc.perform(withHeaders(get("/roles"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    MvcResult roles = mockMvc.perform(withHeaders(get("/roles"), "tenant-a", "iam.role.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode rolesNode = objectMapper.readTree(roles.getResponse().getContentAsString());
    String roleId = rolesNode.path("data").get(0).path("id").asText();

    String bindBody = """
      {
        "permissionKeys": ["iam.user.read", "iam.perm.read"]
      }
      """;
    mockMvc.perform(withHeaders(put("/roles/" + roleId + "/permissions"), "tenant-a", "iam.role.perm.bind")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bindBody))
      .andExpect(status().isOk());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean found = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("IAM_ROLE_PERMISSION_BIND".equals(item.path("action").asText())) {
        found = true;
        break;
      }
    }
    assertThat(found).isTrue();
  }

  @Test
  void accessGrantIssuanceValidatesScopeAndIsIdempotent() throws Exception {
    MvcResult users = mockMvc.perform(withHeaders(get("/users"), "tenant-a", "iam.user.read"))
      .andExpect(status().isOk())
      .andReturn();
    String userId = objectMapper.readTree(users.getResponse().getContentAsString()).path("data").get(0).path("id").asText();

    String invalidGrant = """
      {
        "userId": "%s",
        "action": "OPEN_SLOT"
      }
      """.formatted(userId);
    mockMvc.perform(withHeaders(post("/access-grants"), "tenant-a", "iam.access-grant.issue")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidGrant))
      .andExpect(status().isBadRequest());

    String validGrant = """
      {
        "userId": "%s",
        "grantType": "ORDER_SCOPED",
        "action": "OPEN_SLOT",
        "orderId": "ORDER-ITER02-001",
        "cabinetId": "CAB-A1",
        "slotId": "S1",
        "ttlSeconds": 300
      }
      """.formatted(userId);

    MockHttpServletRequestBuilder issue = withHeaders(post("/access-grants"), "tenant-a", "iam.access-grant.issue")
      .header("Idempotency-Key", "idem-abac-grant-iter02")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validGrant);

    JsonNode first = objectMapper.readTree(mockMvc.perform(issue).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(issue).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertThat(first.at("/data/id").asText()).isEqualTo(second.at("/data/id").asText());
    assertThat(first.at("/data/token").asText()).startsWith("grant:tenant-a:");
  }

  @Test
  void staffCreateEnforcesPermissionIsolationIdempotencyAndAudit() throws Exception {
    mockMvc.perform(withHeaders(get("/staff"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    String createBody = """
      {
        "staffType": "TECHNICIAN",
        "employeeCode": "TECH-ITER04-001",
        "displayName": "迭代技师",
        "mobile": "13812345678"
      }
      """;

    MockHttpServletRequestBuilder create = withHeaders(post("/staff"), "tenant-a", "workforce.staff.create")
      .header("Idempotency-Key", "idem-staff-create-iter04")
      .contentType(MediaType.APPLICATION_JSON)
      .content(createBody);

    JsonNode firstCreate = objectMapper.readTree(mockMvc.perform(create).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode secondCreate = objectMapper.readTree(mockMvc.perform(create).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

    String staffId = firstCreate.at("/data/staffId").asText();
    assertThat(staffId).isEqualTo(secondCreate.at("/data/staffId").asText());
    assertThat(firstCreate.at("/data/mobileMasked").asText()).isEqualTo("138****5678");

    mockMvc.perform(withHeaders(get("/staff/" + staffId), "tenant-a", "workforce.staff.read"))
      .andExpect(status().isOk());

    mockMvc.perform(withHeaders(get("/staff/" + staffId), "tenant-b", "workforce.staff.read"))
      .andExpect(status().isNotFound());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();

    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("WORKFORCE_STAFF_CREATE".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  @Test
  void otaCommandIsIdempotentAndWritesAudit() throws Exception {
    String payload = """
      {
        "targetVersion": "v5.4.1",
        "packageUrl": "https://example.com/ota-v5.4.1.bin"
      }
      """;

    MockHttpServletRequestBuilder request = withHeaders(
      post("/iot/cabinets/CAB-A1/commands/ota"),
      "tenant-a",
      "iot.open-slot.execute"
    )
      .header("Idempotency-Key", "idem-ota-command-1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    JsonNode first = objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

    assertThat(first.at("/data/commandId").asText()).isEqualTo(second.at("/data/commandId").asText());
    assertThat(first.at("/data/commandType").asText()).isEqualTo("OTA");
    assertThat(first.at("/data/targetVersion").asText()).isEqualTo("v5.4.1");

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());

    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("IOT_OTA_COMMAND".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  @Test
  void remoteCommandRequiresPermissionAndSupportsIdempotency() throws Exception {
    String payload = """
      {
        "commandName": "REBOOT",
        "args": {
          "delaySec": 5
        }
      }
      """;

    mockMvc.perform(withHeaders(post("/iot/cabinets/CAB-A1/commands/remote"), "tenant-a", "")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isForbidden());

    MockHttpServletRequestBuilder request = withHeaders(
      post("/iot/cabinets/CAB-A1/commands/remote"),
      "tenant-a",
      "iot.open-slot.execute"
    )
      .header("Idempotency-Key", "idem-remote-command-1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload);

    JsonNode first = objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

    assertThat(first.at("/data/commandId").asText()).isEqualTo(second.at("/data/commandId").asText());
    assertThat(first.at("/data/commandType").asText()).isEqualTo("REMOTE");
    assertThat(first.at("/data/commandName").asText()).isEqualTo("REBOOT");
  }

  @Test
  void appTechNotificationPrefsEnforcePermissionIsolationIdempotencyAndAudit() throws Exception {
    mockMvc.perform(get("/app/tech/notification-prefs")
        .header("Authorization", "Bearer mock-jwt:tenant-a:tech01"))
      .andExpect(status().isForbidden());

    JsonNode tenantARead = objectMapper.readTree(mockMvc.perform(get("/app/tech/notification-prefs")
        .header("Authorization", "Bearer mock-jwt:tenant-a:tech01")
        .header("X-Permissions", "app.tech.notifications.read"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(tenantARead.at("/data/tenantId").asText()).isEqualTo("tenant-a");
    assertThat(tenantARead.at("/data/pushEnabled").asBoolean()).isTrue();

    String updateBody = """
      {
        "pushEnabled": false,
        "quietHoursEnabled": true,
        "quietHoursStart": "23:30",
        "quietHoursEnd": "06:30"
      }
      """;

    MockHttpServletRequestBuilder update = put("/app/tech/notification-prefs")
      .header("Authorization", "Bearer mock-jwt:tenant-a:tech01")
      .header("X-Permissions", "app.tech.notifications.update")
      .header("Idempotency-Key", "idem-app-tech-prefs-iter16")
      .contentType(MediaType.APPLICATION_JSON)
      .content(updateBody);

    JsonNode first = objectMapper.readTree(mockMvc.perform(update).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    JsonNode second = objectMapper.readTree(mockMvc.perform(update).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

    assertThat(first.at("/data/pushEnabled").asBoolean()).isFalse();
    assertThat(first.at("/data/quietHoursEnabled").asBoolean()).isTrue();
    assertThat(first.at("/data/requestId").asText()).isEqualTo(second.at("/data/requestId").asText());

    JsonNode tenantBRead = objectMapper.readTree(mockMvc.perform(get("/app/tech/notification-prefs")
        .header("Authorization", "Bearer mock-jwt:tenant-b:tech01")
        .header("X-Permissions", "app.tech.notifications.read"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(tenantBRead.at("/data/tenantId").asText()).isEqualTo("tenant-b");
    assertThat(tenantBRead.at("/data/pushEnabled").asBoolean()).isTrue();

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("APP_TECH_NOTIFICATION_PREFS_UPDATE".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  @Test
  void financeSettlementApisEnforcePermissionIsolationIdempotencyAndAudit() throws Exception {
    mockMvc.perform(withHeaders(get("/finance/settlements"), "tenant-a", ""))
      .andExpect(status().isForbidden());

    JsonNode listNode = objectMapper.readTree(mockMvc.perform(withHeaders(
        get("/finance/settlements"),
        "tenant-a",
        "finance.settlement.read"
      ))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());

    assertThat(listNode.at("/total").asLong()).isGreaterThan(0L);
    String settlementId = listNode.at("/data/0/settlementId").asText();
    assertThat(settlementId).isNotBlank();

    mockMvc.perform(withHeaders(get("/finance/settlements/" + settlementId), "tenant-a", "finance.settlement.read"))
      .andExpect(status().isOk());

    mockMvc.perform(withHeaders(get("/finance/settlements/" + settlementId), "tenant-b", "finance.settlement.read"))
      .andExpect(status().isNotFound());

    JsonNode linesNode = objectMapper.readTree(mockMvc.perform(withHeaders(
        get("/finance/settlements/" + settlementId + "/lines"),
        "tenant-a",
        "finance.settlement.read"
      ))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    assertThat(linesNode.at("/total").asLong()).isGreaterThan(0L);

    String exportBody = """
      {
        "format": "CSV"
      }
      """;
    MockHttpServletRequestBuilder exportRequest = withHeaders(
      post("/finance/settlements/" + settlementId + "/export"),
      "tenant-a",
      "finance.settlement.export,audit.log.read"
    )
      .header("Idempotency-Key", "idem-finance-settlement-export-iter17")
      .contentType(MediaType.APPLICATION_JSON)
      .content(exportBody);

    JsonNode firstExport = objectMapper.readTree(mockMvc.perform(exportRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());
    JsonNode secondExport = objectMapper.readTree(mockMvc.perform(exportRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString());

    assertThat(firstExport.at("/data/exportId").asText()).isEqualTo(secondExport.at("/data/exportId").asText());
    assertThat(firstExport.at("/data/downloadUrl").asText()).isEqualTo(secondExport.at("/data/downloadUrl").asText());
    assertThat(firstExport.at("/data/requestId").asText()).isEqualTo(secondExport.at("/data/requestId").asText());

    MvcResult logs = mockMvc.perform(withHeaders(get("/audit/logs"), "tenant-a", "audit.log.read"))
      .andExpect(status().isOk())
      .andReturn();
    JsonNode logsNode = objectMapper.readTree(logs.getResponse().getContentAsString());
    boolean foundAudit = false;
    for (JsonNode item : logsNode.path("data")) {
      if ("SETTLEMENT_EXPORT_CREATE".equals(item.path("action").asText())) {
        foundAudit = true;
        break;
      }
    }
    assertThat(foundAudit).isTrue();
  }

  private MockHttpServletRequestBuilder withHeaders(
    MockHttpServletRequestBuilder builder,
    String tenantId,
    String permissions
  ) {
    return builder
      .header("Authorization", "Bearer mock-jwt:" + tenantId + ":tester")
      .header("X-Tenant-Id", tenantId)
      .header("X-User-Id", "tester")
      .header("X-Request-Id", "req-" + System.nanoTime())
      .header("X-Permissions", permissions);
  }
}
