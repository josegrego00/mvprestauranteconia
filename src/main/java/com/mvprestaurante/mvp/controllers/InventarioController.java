package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.InventarioItemDTO;
import com.mvprestaurante.mvp.DTO.InventarioReporteDTO;
import com.mvprestaurante.mvp.services.InventarioService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public String lista(Model model) {
        List<InventarioItemDTO> items = inventarioService.obtenerItemsInventario();
        model.addAttribute("items", items);
        return "inventario/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            inventarioService.guardarInventario(allParams);
            ra.addFlashAttribute("success", "Inventario guardado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventario";
    }

    @GetMapping("/reporte")
    public String reporte(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        if (fechaDesde == null) {
            fechaDesde = LocalDate.now();
        }
        if (fechaHasta == null) {
            fechaHasta = LocalDate.now();
        }

        Page<InventarioReporteDTO> reporte = inventarioService.obtenerReporte(fechaDesde, fechaHasta, page, 20);

        model.addAttribute("reporte", reporte.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reporte.getTotalPages());
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "inventario/reporte";
    }

    @GetMapping("/descargar-plantilla")
    public ResponseEntity<byte[]> descargarPlantilla() throws Exception {
        List<InventarioItemDTO> items = inventarioService.obtenerItemsInventario();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Tipo");
        headerRow.createCell(1).setCellValue("Nombre");
        headerRow.createCell(2).setCellValue("Unidad");
        headerRow.createCell(3).setCellValue("Stock Sistema");
        headerRow.createCell(4).setCellValue("Stock Físico");

        int rowNum = 1;
        for (InventarioItemDTO item : items) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getTipo());
            row.createCell(1).setCellValue(item.getNombre());
            row.createCell(2).setCellValue(item.getUnidadMedida());
            row.createCell(3).setCellValue(item.getStockSistema());
            row.createCell(4).setCellValue(0.0);
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "inventario_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(baos.toByteArray());
    }
}
