package br.minhasfinancas.backend.service;

import br.minhasfinancas.backend.dto.ExtractedItemDTO;
import br.minhasfinancas.backend.dto.ExtractedNfceDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NfceParserService {

    public ExtractedNfceDTO parseHtml(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        ExtractedNfceDTO dto = new ExtractedNfceDTO();

        extractEstablishmentInfo(doc, dto);
        extractGeneralInfo(doc, dto);
        extractItems(doc, dto);
        extractTotals(doc, dto);

        return dto;
    }

    private void extractEstablishmentInfo(Document doc, ExtractedNfceDTO dto) {
        Element nameElement = doc.selectFirst("#u20.txtTopo");
        if (nameElement != null) {
            dto.setEstablishmentName(nameElement.text().trim());
        }

        Elements texts = doc.select("#conteudo .txtCenter .text");
        for (Element text : texts) {
            if (text.text().contains("CNPJ:")) {
                String cnpjCru = text.text().replace("CNPJ:", "").trim();
                dto.setCnpj(cnpjCru.replaceAll("[^0-9]", ""));
                break;
            }
        }
    }

    private void extractGeneralInfo(Document doc, ExtractedNfceDTO dto) {
        Element chaveElement = doc.selectFirst("span.chave");
        if (chaveElement != null) {
            dto.setAccessKey(chaveElement.text().replaceAll("\\s+", ""));
        }

        Element emissaoElement = doc.selectFirst("#infos ul li");
        if (emissaoElement != null) {
            String text = emissaoElement.text();
            Pattern pattern = Pattern.compile("Emissão:\\s+(\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2})");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dataStr = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                dto.setEmissionDate(LocalDateTime.parse(dataStr, formatter));
            }
        }
    }

    private void extractItems(Document doc, ExtractedNfceDTO dto) {
        List<ExtractedItemDTO> items = new ArrayList<>();
        Elements rows = doc.select("table#tabResult tr[id^='Item']");

        for (Element row : rows) {
            ExtractedItemDTO item = new ExtractedItemDTO();

            item.setName(Objects.requireNonNull(row.select("span.txtTit").first()).text().trim());

            String codeRaw = row.select("span.RCod").text();
            item.setCode(codeRaw.replaceAll("[^0-9]", ""));

            String qtdRaw = row.select("span.Rqtd").text().replace("Qtde.:", "").trim();
            item.setQuantity(parseBigDecimalBR(qtdRaw));

            String unitRaw = row.select("span.RUN").text().replace("UN:", "").trim();
            item.setUnit(unitRaw);

            String vlUnitRaw = row.select("span.RvlUnit").text().replace("Vl. Unit.:", "").trim();
            item.setUnitPrice(parseBigDecimalBR(vlUnitRaw));

            String vlTotalRaw = row.select("td.txtTit.noWrap span.valor").text().trim();
            item.setTotalPrice(parseBigDecimalBR(vlTotalRaw));

            items.add(item);
        }
        dto.setItems(items);
    }

    private void extractTotals(Document doc, ExtractedNfceDTO dto) {
        Elements totalLines = doc.select("#totalNota #linhaTotal");
        for (Element line : totalLines) {
            String label = line.select("label").text();
            String value = line.select("span.totalNumb").text();

            if (label.contains("Valor a pagar R$:")) {
                dto.setTotalValue(parseBigDecimalBR(value));
            } else if (label.contains("Descontos R$:")) {
                dto.setDiscount(parseBigDecimalBR(value));
            }
        }

        if (dto.getDiscount() == null) {
            dto.setDiscount(BigDecimal.ZERO);
        }

        if (dto.getTotalValue() == null) {
            dto.setTotalValue(BigDecimal.ZERO);
        }
    }

    private BigDecimal parseBigDecimalBR(String value) {
        if (value == null || value.trim().isEmpty() || value.contains("NaN")) {
            return BigDecimal.ZERO;
        }
        String normalized = value.replace(".", "").replace(",", ".");
        return new BigDecimal(normalized);
    }
}