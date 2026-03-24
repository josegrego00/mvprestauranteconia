package com.mvprestaurante.mvp.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(
            DuplicateResourceException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(
            BusinessException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(
            Exception ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado: " + ex.getMessage());
        return getRedirectUrl(request);
    }
}