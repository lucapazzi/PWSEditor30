package utility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * PDFExporter: Exports a JPanel to a PDF file using Apache PDFBox.
 * Renders panel to high-resolution BufferedImage for consistent quality.
 */
public class PDFExporter {

    /**
     * Export a JPanel to a PDF file with high quality.
     * @param panel The panel to export
     * @param file The destination PDF file
     * @throws IOException if writing fails
     */
    public static void exportPanelToPDF(JPanel panel, File file) throws IOException {
        // Get panel dimensions
        int w = panel.getWidth() > 0 ? panel.getWidth() : panel.getPreferredSize().width;
        int h = panel.getHeight() > 0 ? panel.getHeight() : panel.getPreferredSize().height;
        
        // Render to high-resolution BufferedImage (3x for quality)
        int scale = 3;
        int imgW = w * scale;
        int imgH = h * scale;
        
        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        
        // High quality rendering hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        // Scale for high resolution
        g2.scale(scale, scale);
        
        // White background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        
        // Paint the panel
        panel.printAll(g2);
        g2.dispose();
        
        // Create PDF with scaled dimensions
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(w, h));
            document.addPage(page);

            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // Create image from byte array and embed in PDF
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, "diagram");

            // Draw image on page at original size
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0, w, h);
            }

            // Save document
            document.save(file);
        }
    }
}
