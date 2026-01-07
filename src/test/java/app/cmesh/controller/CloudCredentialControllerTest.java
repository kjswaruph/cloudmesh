package app.cmesh.controller;

import app.cmesh.credentials.CloudCredentialDTO;
import app.cmesh.credentials.CloudCredentialService;
import app.cmesh.credentials.ValidationResult;
import app.cmesh.dashboard.CloudCredentials.CredentialStatus;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.user.User;
import app.cmesh.user.UserRepository;
import app.cmesh.credentials.dto.ValidationResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudCredentialControllerTest {

        @Mock
        private CloudCredentialService credentialService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private Authentication authentication;

        @Mock
        private UserDetails userDetails;

        @InjectMocks
        private CloudCredentialController controller;

        private UUID testUserId;
        private UUID testCredentialId;
        private User testUser;
        private CloudCredentialDTO testCredential;

        @BeforeEach
        void setUp() {
                testUserId = UUID.randomUUID();
                testCredentialId = UUID.randomUUID();

                testUser = new User();
                testUser.setUserId(testUserId);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");

                testCredential = new CloudCredentialDTO(
                                testCredentialId,
                                CloudProvider.AWS,
                                "Test AWS Account",
                                CredentialStatus.ACTIVE,
                                "us-east-1",
                                Instant.now(),
                                Instant.now(),
                                Instant.now());
        }

        // ==================== QUERY TESTS ====================

        @Test
        void testCloudCredentials_ListAll() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.listCredentials(testUserId)).thenReturn(Arrays.asList(testCredential));

                // Act
                List<CloudCredentialDTO> result = controller.cloudCredentials(null, authentication);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(testCredentialId, result.get(0).credentialId());
                verify(credentialService).listCredentials(testUserId);
        }

        @Test
        void testCloudCredentials_FilterByProvider() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.listCredentialsByProvider(testUserId, CloudProvider.AWS))
                                .thenReturn(Arrays.asList(testCredential));

                // Act
                List<CloudCredentialDTO> result = controller.cloudCredentials(CloudProvider.AWS, authentication);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(CloudProvider.AWS, result.get(0).provider());
                verify(credentialService).listCredentialsByProvider(testUserId, CloudProvider.AWS);
        }

        @Test
        void testCloudCredential_Success() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.getCredential(testCredentialId, testUserId))
                                .thenReturn(Optional.of(testCredential));

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.cloudCredential(testCredentialId.toString(),
                                authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(testCredentialId, response.getBody().credentialId());
                verify(credentialService).getCredential(testCredentialId, testUserId);
        }

        @Test
        void testCloudCredential_NotFound() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.getCredential(testCredentialId, testUserId))
                                .thenReturn(Optional.empty());

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.cloudCredential(testCredentialId.toString(),
                                authentication);

                // Assert
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        // ==================== MUTATION TESTS - AWS ====================

        @Test
        void testConnectAwsAccount_Success() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.createCredential(eq(testUserId), eq(CloudProvider.AWS), anyString(), anyMap(),
                                eq(true)))
                                .thenReturn(testCredential);

                // Create valid AWS input using a mock implementation
                var awsInput = new app.cmesh.credentials.graphql.AwsCredentialInput(
                                "Test AWS",
                                "arn:aws:iam::123456789012:role/TestRole",
                                "external-id-123",
                                "us-east-1");

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.connectAwsAccount(awsInput, authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(CloudProvider.AWS, response.getBody().provider());
                verify(credentialService).createCredential(eq(testUserId), eq(CloudProvider.AWS), anyString(), anyMap(),
                                eq(true));
        }

        // ==================== MUTATION TESTS - GCP ====================

        @Test
        void testConnectGcpAccount_Success() {
                // Arrange
                CloudCredentialDTO gcpCredential = new CloudCredentialDTO(
                                UUID.randomUUID(),
                                CloudProvider.GCP,
                                "Test GCP Account",
                                CredentialStatus.ACTIVE,
                                "us-central1",
                                Instant.now(),
                                Instant.now(),
                                Instant.now());

                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.createCredential(eq(testUserId), eq(CloudProvider.GCP), anyString(), anyMap(),
                                eq(true)))
                                .thenReturn(gcpCredential);

                var gcpInput = new app.cmesh.credentials.graphql.GcpCredentialInput(
                                "Test GCP",
                                "{\"type\":\"service_account\"}",
                                "my-project",
                                "us-central1");

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.connectGcpAccount(gcpInput, authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(CloudProvider.GCP, response.getBody().provider());
        }

        // ==================== MUTATION TESTS - Azure ====================

        @Test
        void testConnectAzureAccount_Success() {
                // Arrange
                CloudCredentialDTO azureCredential = new CloudCredentialDTO(
                                UUID.randomUUID(),
                                CloudProvider.AZURE,
                                "Test Azure Account",
                                CredentialStatus.ACTIVE,
                                "eastus",
                                Instant.now(),
                                Instant.now(),
                                Instant.now());

                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.createCredential(eq(testUserId), eq(CloudProvider.AZURE), anyString(), anyMap(),
                                eq(true)))
                                .thenReturn(azureCredential);

                var azureInput = new app.cmesh.credentials.graphql.AzureCredentialInput(
                                "Test Azure",
                                "12345678-1234-1234-1234-123456789012",
                                "client-secret",
                                "12345678-1234-1234-1234-123456789012",
                                "12345678-1234-1234-1234-123456789012",
                                "eastus");

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.connectAzureAccount(azureInput,
                                authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(CloudProvider.AZURE, response.getBody().provider());
        }

        // ==================== MUTATION TESTS - DigitalOcean ====================

        @Test
        void testConnectDigitalOceanAccount_Success() {
                // Arrange
                CloudCredentialDTO doCredential = new CloudCredentialDTO(
                                UUID.randomUUID(),
                                CloudProvider.DIGITALOCEAN,
                                "Test DO Account",
                                CredentialStatus.ACTIVE,
                                "nyc3",
                                Instant.now(),
                                Instant.now(),
                                Instant.now());

                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.createCredential(eq(testUserId), eq(CloudProvider.DIGITALOCEAN), anyString(),
                                anyMap(), eq(true)))
                                .thenReturn(doCredential);

                var doInput = new app.cmesh.credentials.graphql.DigitalOceanCredentialInput(
                                "Test DO",
                                "a".repeat(64), // Valid token length
                                "nyc3");

                // Act
                ResponseEntity<CloudCredentialDTO> response = controller.connectDigitalOceanAccount(doInput,
                                authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(CloudProvider.DIGITALOCEAN, response.getBody().provider());
        }

        // ==================== MUTATION TESTS - Credential Management
        // ====================

        @Test
        void testValidateCredential_Success() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.validateCredential(testCredentialId, testUserId))
                                .thenReturn(ValidationResult.success("Validation successful"));

                // Act
                ResponseEntity<ValidationResultDTO> response = controller
                                .validateCredential(testCredentialId.toString(), authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().valid());
                assertEquals("Validation successful", response.getBody().message());
        }

        @Test
        void testValidateCredential_Failed() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                when(credentialService.validateCredential(testCredentialId, testUserId))
                                .thenReturn(ValidationResult.failure("Invalid credentials"));

                // Act
                ResponseEntity<ValidationResultDTO> response = controller
                                .validateCredential(testCredentialId.toString(), authentication);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertFalse(response.getBody().valid());
                assertEquals("Invalid credentials", response.getBody().message());
        }

        @Test
        void testDeleteCredential_Success() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                doNothing().when(credentialService).deleteCredential(testCredentialId, testUserId);

                // Act
                ResponseEntity<Void> response = controller.deleteCredential(testCredentialId.toString(),
                                authentication);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(credentialService).deleteCredential(testCredentialId, testUserId);
        }

        @Test
        void testDeleteCredential_Failed() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);
                doThrow(new IllegalArgumentException("Credential not found"))
                                .when(credentialService).deleteCredential(testCredentialId, testUserId);

                // Act
                ResponseEntity<Void> response = controller.deleteCredential(testCredentialId.toString(),
                                authentication);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        // ==================== EDGE CASES ====================

        @Test
        void testCloudCredential_InvalidUUID() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("testuser");
                when(userRepository.findUsersByUsername("testuser")).thenReturn(testUser);

                // Act & Assert
                // The parser helper throws IllegalArgumentException
                assertThrows(IllegalArgumentException.class,
                                () -> controller.cloudCredential("invalid-uuid", authentication));
        }

        @Test
        void testGetUserId_UserNotFound() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(userDetails.getUsername()).thenReturn("nonexistent");
                when(userRepository.findUsersByUsername("nonexistent")).thenReturn(null);

                // Act & Assert
                assertThrows(IllegalStateException.class, () -> controller.cloudCredentials(null, authentication));
        }

        @Test
        void testGetUserId_NullAuthentication() {
                // Act & Assert
                assertThrows(IllegalStateException.class, () -> controller.cloudCredentials(null, null));
        }
}
