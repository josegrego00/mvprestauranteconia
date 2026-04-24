package com.mvprestaurante.mvp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isRestRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        return accept != null && accept.contains("application/json")
                || "XMLHttpRequest".equals(xRequestedWith);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicate(
            DuplicateResourceException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (isRestRequest(request)) {
            return createErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(
            BusinessException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (isRestRequest(request)) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        if (isRestRequest(request)) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return "redirect:/error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        if (isRestRequest(request)) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }

        return "redirect:/error";
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}