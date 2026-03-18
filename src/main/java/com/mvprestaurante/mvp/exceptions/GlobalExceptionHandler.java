package com.mvprestaurante.mvp.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestHeader;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(
            DuplicateResourceException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());

        String referer = request.getHeader("Referer");

        if (referer != null) {
            return "redirect:" + referer;
        }

        return "redirect:/ingredientes";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(
            BusinessException ex,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/ingredientes";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(
            Exception ex,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado");
        return "redirect:/ingredientes";
    }
}