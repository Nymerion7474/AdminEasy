// src/main/java/com/admineasy/service/OcrService.java
package com.admineasy.service;

import com.admineasy.dto.ContractDto;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        // 1) Chemin vers le dossier tessdata (à adapter selon ton install)
        //    Sur Windows par exemple : "C:/Program Files/Tesseract-OCR/tessdata"
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        // 2) Langue par défaut : français + anglais
        tesseract.setLanguage("fra+eng");
        // Optionnel : tesseract.setOcrEngineMode(...);
    }

    /**
     * Prend un fichier (PDF/PNG/JPG) fait de l'OCR, retourne un ContractDto rempli au maximum.
     */
    public ContractDto parseContract(MultipartFile multipartFile) throws IOException, TesseractException {
        // 1. Enregistrer le fichier temporaire
        File temp = File.createTempFile("contract_upload_", "_" + multipartFile.getOriginalFilename());
        Files.copy(multipartFile.getInputStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // 2. Extraire le texte brut
        String ocrResult = tesseract.doOCR(temp);

        // 3. Supprimer le fichier temporaire
        temp.deleteOnExit();

        // 4. Parser les champs avec des regex simples :
        String contractNumber = extractByRegex(ocrResult, "(?i)Contrat\\s*(?:n°\\s*:?)?\\s*([A-Za-z0-9\\-_/]+)");
        String name = extractByRegex(ocrResult, "(?i)Objet\\s*[:–\\-]?\\s*(.+)");
        LocalDate startDate = extractDate(ocrResult, "Date\\s*(?:de\\s*d[ée]but)\\s*[:–\\-]?\\s*(\\d{2}/\\d{2}/\\d{4}|\\d{4}-\\d{2}-\\d{2})");
        LocalDate endDate = extractDate(ocrResult, "Date\\s*(?:de\\s*f(?:in|inal))\\s*[:–\\-]?\\s*(\\d{2}/\\d{2}/\\d{4}|\\d{4}-\\d{2}-\\d{2})");
        BigDecimal amount = extractAmount(ocrResult);
        String currency = (amount != null && ocrResult.contains("€")) ? "EUR" : null;
        String contractType = extractByRegex(ocrResult, "(?i)Type\\s*[:–\\-]?\\s*(.+)");
        String paymentFrequency = extractByRegex(ocrResult, "(?i)Fr[ée]quence\\s*[:–\\-]?\\s*(Mensuelle|Annuel|Unique|Trimestrielle)");
        boolean autoRenew = ocrResult.matches("(?i).*auto[- ]?renouvellement.*");

        String providerContact = extractByRegex(ocrResult, "(?i)Contact\\s*(?:fournisseur)?\\s*[:–\\-]?\\s*([A-Za-z0-9.%_+\\-@ ]+)");
        String notes = ""; // On laisse vide ou on stocke tout le reste du texte

        // 5. Construire le DTO
        ContractDto dto = new ContractDto();
        dto.setContractNumber(contractNumber);
        dto.setName(name);
        dto.setStartDate(startDate != null ? startDate : null);
        dto.setEndDate(endDate != null ? endDate : null);
        dto.setAmount(amount);
        dto.setCurrency(currency);
        dto.setContractType(contractType);
        dto.setPaymentFrequency(paymentFrequency);
        dto.setAutoRenew(autoRenew);
        dto.setProviderContact(providerContact);
        dto.setNotes(notes);
        // status et id = null, le front gérera

        return dto;
    }

    /**
     * Extrait la première occurence du groupe 1 selon la regex, ou null si pas trouvé
     */
    private String extractByRegex(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Extrait une date (premier groupe) au format dd/MM/yyyy ou yyyy-MM-dd
     */
    private LocalDate extractDate(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String raw = m.group(1).trim();
            try {
                if (raw.contains("/")) {
                    return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extrait le premier montant trouvé (chiffres + séparateur), genre “1 200,50” ou “1200.50”
     */
    private BigDecimal extractAmount(String text) {
        // Recherche ex : “€ 1 200,50” ou “1200.50 EUR”
        Pattern p = Pattern.compile("(?:€|EUR)?\\s*([0-9]{1,3}(?:[\\s\\.][0-9]{3})*(?:,[0-9]{2}|\\.[0-9]{2}))");
        Matcher m = p.matcher(text.replace("\u00A0", " ")); // remplacer espaces insécables
        if (m.find()) {
            String raw = m.group(1).replace(" ", "").replace(".", "").replace(",", ".");
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
