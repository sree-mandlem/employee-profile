package com.company.absence.api;

import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;
import com.company.common.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Absences", description = "Employee absence request operations")
@RequestMapping("/api")
public interface AbsenceRequestApi {

    @Operation(summary = "Get current employee's absence requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Absences retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/absences")
    ResponseEntity<List<AbsenceRequestDto>> getMyAbsences();

    @Operation(summary = "Create an absence request for current employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Absence request created"),
            @ApiResponse(responseCode = "400", description = "Invalid input (date range, type, etc.)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/me/absences")
    ResponseEntity<AbsenceRequestDto> requestAbsence(
            @Valid @RequestBody AbsenceRequestCreateRequest request);

    @Operation(summary = "Get pending absence requests for manager's team")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending absence requests retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not a manager)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/managers/me/absences")
    ResponseEntity<List<AbsenceRequestDto>> getPendingAbsencesForMyTeam();

    @Operation(summary = "Approve an absence request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Absence approved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not manager)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Absence request not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/absences/{id}/approve")
    ResponseEntity<AbsenceRequestDto> approveAbsence(@PathVariable Long id);

    @Operation(summary = "Reject an absence request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Absence rejected"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not manager)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Absence request not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/absences/{id}/reject")
    ResponseEntity<AbsenceRequestDto> rejectAbsence(@PathVariable Long id);

    @Operation(summary = "Cancel an absence request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Absence cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not manager)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Absence request not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/absences/{id}/cancel")
    ResponseEntity<AbsenceRequestDto> cancelAbsence(@PathVariable Long id);
}
