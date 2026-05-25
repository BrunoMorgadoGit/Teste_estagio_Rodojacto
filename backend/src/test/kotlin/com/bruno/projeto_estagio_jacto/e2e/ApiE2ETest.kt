package com.bruno.projeto_estagio_jacto.e2e

import com.bruno.projeto_estagio_jacto.dto.auth.AuthRequest
import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.entity.Device
import com.bruno.projeto_estagio_jacto.entity.Organization
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.repository.DeviceRepository
import com.bruno.projeto_estagio_jacto.repository.OrganizationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository

    @Autowired
    private lateinit var collaboratorRepository: CollaboratorRepository

    @Autowired
    private lateinit var deviceRepository: DeviceRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var organizationA: Organization
    private lateinit var organizationB: Organization
    private lateinit var manager: Collaborator
    private lateinit var operator: Collaborator
    private lateinit var foreignOperator: Collaborator

    @BeforeEach
    fun setUp() {
        deviceRepository.deleteAll()
        collaboratorRepository.deleteAll()
        organizationRepository.deleteAll()

        organizationA = organizationRepository.save(
            Organization(corporateName = "Organization A", registrationCode = "ORG-A"),
        )
        organizationB = organizationRepository.save(
            Organization(corporateName = "Organization B", registrationCode = "ORG-B"),
        )

        manager = collaboratorRepository.save(
            Collaborator(
                fullName = "Manager",
                email = "manager@jacto.com",
                password = passwordEncoder.encode("Admin@123"),
                accessLevel = AccessLevel.MANAGER,
                organization = organizationA,
            ),
        )
        operator = collaboratorRepository.save(
            Collaborator(
                fullName = "Operator A",
                email = "operator@jacto.com",
                password = passwordEncoder.encode("Admin@123"),
                accessLevel = AccessLevel.OPERATOR,
                organization = organizationA,
            ),
        )
        foreignOperator = collaboratorRepository.save(
            Collaborator(
                fullName = "Operator B",
                email = "operatorb@jacto.com",
                password = passwordEncoder.encode("Admin@123"),
                accessLevel = AccessLevel.OPERATOR,
                organization = organizationB,
            ),
        )

        deviceRepository.save(Device(model = "Device A1", assetTag = "TAG-A1", organization = organizationA))
        deviceRepository.save(Device(model = "Device B1", assetTag = "TAG-B1", organization = organizationB))
    }

    @Test
    fun `should return 401 when requesting protected routes without token`() {
        listOf(
            "/api/auth/me",
            "/api/organizations",
            "/api/collaborators",
            "/api/devices",
            "/api/dashboard/summary",
        ).forEach { route ->
            mockMvc.perform(get(route))
                .andExpect(status().isUnauthorized)
        }
    }

    @Test
    fun `should return 401 when token user no longer exists`() {
        val operatorToken = loginAndGetToken("operatorb@jacto.com", "Admin@123")
        collaboratorRepository.delete(foreignOperator)

        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should allow access to swagger docs without token`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openapi").exists())
    }

    @Test
    fun `should login successfully and return bearer token`() {
        val response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest("manager@jacto.com", "Admin@123"))),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn()

        val body = response.response.contentAsString
        assertThat(body).contains("accessToken")
    }

    @Test
    fun `should return current authenticated user`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("manager@jacto.com"))
            .andExpect(jsonPath("$.accessLevel").value("MANAGER"))
            .andExpect(jsonPath("$.organizationId").value(organizationA.id))
    }

    @Test
    fun `should return 401 when login credentials are invalid`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest("manager@jacto.com", "wrong-password"))),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
    }

    @Test
    fun `manager should list all organizations while operator should see only its own`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/organizations")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        mockMvc.perform(
            get("/api/organizations")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(organizationA.id))
    }

    @Test
    fun `manager should get organization by id`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/organizations/${organizationA.id}")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(organizationA.id))
            .andExpect(jsonPath("$.corporateName").value("Organization A"))
            .andExpect(jsonPath("$.registrationCode").value("ORG-A"))
    }

    @Test
    fun `manager should get global dashboard summary while operator should get organization summary`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/dashboard/summary")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalOrganizations").value(2))
            .andExpect(jsonPath("$.totalCollaborators").value(3))
            .andExpect(jsonPath("$.totalDevices").value(2))
            .andExpect(jsonPath("$.devicesByOrganization.length()").value(2))
            .andExpect(jsonPath("$.collaboratorsByOrganization.length()").value(2))

        mockMvc.perform(
            get("/api/dashboard/summary")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.organizationId").value(organizationA.id))
            .andExpect(jsonPath("$.organizationName").value("Organization A"))
            .andExpect(jsonPath("$.totalCollaborators").value(2))
            .andExpect(jsonPath("$.totalDevices").value(1))
            .andExpect(jsonPath("$.totalManagers").value(1))
            .andExpect(jsonPath("$.totalOperators").value(1))
    }

    @Test
    fun `operator should receive 403 when accessing another organization by id`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/organizations/${organizationB.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `operator should receive 404 when organization does not exist`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/organizations/999999")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Organization with id 999999 not found"))
    }

    @Test
    fun `operator should see only collaborators and devices from its own organization`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/collaborators")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].organizationId").value(organizationA.id))

        mockMvc.perform(
            get("/api/devices")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].organizationId").value(organizationA.id))
    }

    @Test
    fun `operator should receive 403 when filtering collaborators and devices from another organization`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/collaborators")
                .param("organizationId", organizationB.id.toString())
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/devices")
                .param("organizationId", organizationB.id.toString())
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `operator should receive 403 when accessing collaborator and device from another organization by id`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")
        val foreignDevice = deviceRepository.findAll().first { it.assetTag == "TAG-B1" }

        mockMvc.perform(
            get("/api/collaborators/${foreignOperator.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/devices/${foreignDevice.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `manager should create organization successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/organizations")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "Organization C",
                            "registrationCode" to "ORG-C",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.corporateName").value("Organization C"))
            .andExpect(jsonPath("$.registrationCode").value("ORG-C"))
    }

    @Test
    fun `operator should not create organization`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/organizations")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "Forbidden Org",
                            "registrationCode" to "ORG-FORBIDDEN",
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You do not have permission to perform this operation"))
    }

    @Test
    fun `operator should not update or delete records`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")
        val foreignDevice = deviceRepository.findAll().first { it.assetTag == "TAG-B1" }

        mockMvc.perform(
            put("/api/organizations/${organizationB.id}")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "Blocked Organization",
                            "registrationCode" to "ORG-BLOCKED",
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/organizations/${organizationB.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            put("/api/collaborators/${foreignOperator.id}")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "Blocked Collaborator",
                            "email" to "blocked.update@jacto.com",
                            "password" to "Admin@123",
                            "accessLevel" to "OPERATOR",
                            "organizationId" to organizationB.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/collaborators/${foreignOperator.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            put("/api/devices/${foreignDevice.id}")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "Blocked Device",
                            "assetTag" to "BLOCKED-UPDATE",
                            "organizationId" to organizationB.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/devices/${foreignDevice.id}")
                .header("Authorization", "Bearer $operatorToken"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `manager should update organization successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            put("/api/organizations/${organizationA.id}")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "Organization A Updated",
                            "registrationCode" to "ORG-A-UPDATED",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.corporateName").value("Organization A Updated"))
            .andExpect(jsonPath("$.registrationCode").value("ORG-A-UPDATED"))
    }

    @Test
    fun `manager should delete device successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")
        val device = deviceRepository.findAll().first { it.assetTag == "TAG-A1" }

        mockMvc.perform(
            delete("/api/devices/${device.id}")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNoContent)

        assertThat(deviceRepository.findById(device.id!!)).isEmpty
    }

    @Test
    fun `manager should create update and delete collaborator successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        val createResponse = mockMvc.perform(
            post("/api/collaborators")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "New Collaborator",
                            "email" to "new.collaborator@jacto.com",
                            "password" to "Admin@123",
                            "accessLevel" to "OPERATOR",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.fullName").value("New Collaborator"))
            .andExpect(jsonPath("$.email").value("new.collaborator@jacto.com"))
            .andReturn()

        val collaboratorId = objectMapper.readTree(createResponse.response.contentAsString).get("id").asLong()
        val createBody = createResponse.response.contentAsString
        val savedCollaborator = collaboratorRepository.findById(collaboratorId).orElseThrow()

        assertThat(createBody).doesNotContain("password")
        assertThat(savedCollaborator.password).isNotEqualTo("Admin@123")
        assertThat(passwordEncoder.matches("Admin@123", savedCollaborator.password)).isTrue()

        val updateResponse = mockMvc.perform(
            put("/api/collaborators/$collaboratorId")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "Updated Collaborator",
                            "email" to "updated.collaborator@jacto.com",
                            "password" to "Admin@456",
                            "accessLevel" to "MANAGER",
                            "organizationId" to organizationB.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fullName").value("Updated Collaborator"))
            .andExpect(jsonPath("$.email").value("updated.collaborator@jacto.com"))
            .andExpect(jsonPath("$.organizationId").value(organizationB.id))
            .andReturn()

        assertThat(updateResponse.response.contentAsString).doesNotContain("password")

        mockMvc.perform(
            delete("/api/collaborators/$collaboratorId")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNoContent)

        assertThat(collaboratorRepository.findById(collaboratorId)).isEmpty
    }

    @Test
    fun `operator should not create collaborator`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/collaborators")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "Blocked Collaborator",
                            "email" to "blocked@jacto.com",
                            "password" to "Admin@123",
                            "accessLevel" to "OPERATOR",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You do not have permission to perform this operation"))
    }

    @Test
    fun `manager should create update and delete device successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        val createResponse = mockMvc.perform(
            post("/api/devices")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "Device C1",
                            "assetTag" to "TAG-C1",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.model").value("Device C1"))
            .andExpect(jsonPath("$.assetTag").value("TAG-C1"))
            .andReturn()

        val deviceId = objectMapper.readTree(createResponse.response.contentAsString).get("id").asLong()

        mockMvc.perform(
            put("/api/devices/$deviceId")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "Device C2",
                            "assetTag" to "TAG-C2",
                            "organizationId" to organizationB.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.model").value("Device C2"))
            .andExpect(jsonPath("$.assetTag").value("TAG-C2"))
            .andExpect(jsonPath("$.organizationId").value(organizationB.id))

        mockMvc.perform(
            delete("/api/devices/$deviceId")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNoContent)

        assertThat(deviceRepository.findById(deviceId)).isEmpty
    }

    @Test
    fun `operator should not create device`() {
        val operatorToken = loginAndGetToken("operator@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/devices")
                .header("Authorization", "Bearer $operatorToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "Blocked Device",
                            "assetTag" to "BLOCKED-001",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You do not have permission to perform this operation"))
    }

    @Test
    fun `manager should delete organization without related records successfully`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")
        val emptyOrganization = organizationRepository.save(
            Organization(corporateName = "Empty Organization", registrationCode = "ORG-EMPTY"),
        )

        mockMvc.perform(
            delete("/api/organizations/${emptyOrganization.id}")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNoContent)

        assertThat(organizationRepository.findById(emptyOrganization.id!!)).isEmpty
    }

    @Test
    fun `should return 409 when deleting organization with related records`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            delete("/api/organizations/${organizationA.id}")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isConflict)
            .andExpect(
                jsonPath("$.message")
                    .value("Operation cannot be completed because the record is referenced by other data"),
            )
    }

    @Test
    fun `should return 404 when organization does not exist`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/organizations/999999")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Organization with id 999999 not found"))
    }

    @Test
    fun `should return 404 when collaborator does not exist`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/collaborators/999999")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Collaborator with id 999999 not found"))
    }

    @Test
    fun `should return 404 when device does not exist`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            get("/api/devices/999999")
                .header("Authorization", "Bearer $managerToken"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Device with id 999999 not found"))
    }

    @Test
    fun `should return 400 when organization registration code is duplicated`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/organizations")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "Duplicate Org",
                            "registrationCode" to "ORG-A",
                        ),
                    ),
                ),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Registration code already in use"))
    }

    @Test
    fun `should return 409 when collaborator email is duplicated`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/collaborators")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "Duplicate Collaborator",
                            "email" to "manager@jacto.com",
                            "password" to "Admin@123",
                            "accessLevel" to "OPERATOR",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Email already in use"))
    }

    @Test
    fun `should return 409 when device asset tag is duplicated`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/devices")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "Duplicate Device",
                            "assetTag" to "TAG-A1",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Asset tag already in use"))
    }

    @Test
    fun `should return 400 when organization payload is invalid`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/organizations")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "corporateName" to "",
                            "registrationCode" to "",
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.message").value("Invalid request data"))
            .andExpect(jsonPath("$.fields.corporateName").value("Corporate name is required"))
            .andExpect(jsonPath("$.fields.registrationCode").value("Registration code is required"))
    }

    @Test
    fun `should return 400 when collaborator payload is invalid`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/collaborators")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "fullName" to "",
                            "email" to "invalid-email",
                            "password" to "123",
                            "accessLevel" to "OPERATOR",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.message").value("Invalid request data"))
            .andExpect(jsonPath("$.fields.fullName").value("Full name is required"))
            .andExpect(jsonPath("$.fields.email").value("Email must be valid"))
            .andExpect(jsonPath("$.fields.password").value("Password must have at least 6 characters"))
    }

    @Test
    fun `should return 400 when device payload is invalid`() {
        val managerToken = loginAndGetToken("manager@jacto.com", "Admin@123")

        mockMvc.perform(
            post("/api/devices")
                .header("Authorization", "Bearer $managerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "model" to "",
                            "assetTag" to "",
                            "organizationId" to organizationA.id,
                        ),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.message").value("Invalid request data"))
            .andExpect(jsonPath("$.fields.model").value("Model is required"))
            .andExpect(jsonPath("$.fields.assetTag").value("Asset tag is required"))
    }

    private fun loginAndGetToken(email: String, password: String): String {
        val response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest(email, password))),
        )
            .andExpect(status().isOk)
            .andReturn()

        return objectMapper.readTree(response.response.contentAsString).get("accessToken").asText()
    }
}
