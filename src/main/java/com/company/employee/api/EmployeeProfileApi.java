package com.company.employee.api;

import com.company.common.api.ErrorResponse;
import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Employee Profiles", description = "Employee profile operations")
@RequestMapping("/api/employees")
public interface EmployeeProfileApi {

    @Operation(summary = "Get current employee profile (private view)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/profile")
    ResponseEntity<EmployeeProfilePrivateDto> getMyProfile();

    @Operation(summary = "Get private employee profile by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{employeeId}/profile/private")
    ResponseEntity<EmployeeProfilePrivateDto> getPrivateProfile(@PathVariable Long employeeId);

    @Operation(summary = "Get public employee profile by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{employeeId}/profile/public")
    ResponseEntity<EmployeeProfilePublicDto> getPublicProfile(@PathVariable Long employeeId);

    @Operation(summary = "Get all public employee profiles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of public employee profiles",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeProfilePublicDto.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/public")
    ResponseEntity<List<EmployeeProfilePublicDto>> getAllPublicProfiles();

    @Operation(summary = "Update employee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{employeeId}/profile")
    ResponseEntity<EmployeeProfilePrivateDto> updateProfile(@PathVariable Long employeeId,
                                                            @Valid @RequestBody EmployeeProfileUpdateRequest request);
}
