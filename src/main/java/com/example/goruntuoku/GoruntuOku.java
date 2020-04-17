package com.example.goruntuoku;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import org.json.JSONObject;






@RestController
public class GoruntuOku {

    public String extractImage(String filePath) {
        File imageFile = new File(filePath);
        ITesseract instance = new Tesseract();
        instance.setDatapath("Tess4J//tessdata");
        instance.setLanguage("eng");
        try {

            String result = instance.doOCR(imageFile);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return "foto okunamadı";
        }
    }

    public void decoder(String base64Image, String pathFile)
    {
        try (FileOutputStream imageOutFile = new FileOutputStream(pathFile))
        {
            // Converting a Base64 String into Image byte array

            byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
            imageOutFile.write(imageByteArray);

        } catch (FileNotFoundException e) {
            System.out.println("foto bulunamadı" + e);
        } catch (IOException ioe) {
            System.out.println("foto okunamadi " + ioe);
        }
    }

    public String calistir(String gelenb,int row,int col) throws IOException {

        String b64=gelenb;

        decoder(b64, "gelenimage.jpeg");

        File file = new File("gelenimage.jpeg");
        FileInputStream fis = new FileInputStream(file);
        BufferedImage image = ImageIO.read(fis); //reading the image file

        int rows = row; //You should decide the values for rows and cols variables
        int cols = col;
        int chunks = rows * cols;

        int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
        int chunkHeight = image.getHeight() / rows;
        int count = 0;
        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //Initialize the image array with image chunks
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                // draws the image chunk
                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }
        System.out.println("Görüntü bölme tamam");

        //writing mini images into image files
        for (int i = 0; i < imgs.length; i++) {
            ImageIO.write(imgs[i], "jpg", new File("" + i + ".jpeg"));
        }
        System.out.println("ufak fotoğraflar oluşturuldu.");

        String sonuc="";

        if(rows==3 || cols==1) {
            sonuc = extractImage("2.jpeg");
        }

        if(rows==2 || cols==2) {
            sonuc = extractImage("3.jpeg");
        }

        //System.out.println(sonuc);




        String isbn="";

        sonuc=sonuc.replace(":"," ");
        sonuc=sonuc.replace("[","");
        sonuc=sonuc.replace("\n"," \n \n");


        String[] arrOfStr = sonuc.split(" ", 0);

        for (String a : arrOfStr)
        {
            //System.out.println(a+" "+a.length());
            /*
            if(a.matches("^[0-9]*-[0-9]*-[0-9]*-[0-9]*-[0-9]*$"))
            {
                System.out.println("eşleşme var");
                isbn=a;
            }
             */
            if(a.length()==17)
            {
                isbn=a;
            }
        }


        System.out.println("Bulunan isbn:"+isbn);
        return isbn;


    }

    @Configuration
    public class WebfluxConfig implements WebFluxConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

            registry.addResourceHandler("/swagger-ui.html**")
                    .addResourceLocations("classpath:/META-INF/resources/");

            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }

        @Override
        public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
            configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
        }
    }

    @PostMapping(path = "/goruntuoku", consumes = "application/json", produces = "application/json")
    public String GoruntuBilgileri(@RequestBody String gelenler) throws IOException,org.json.JSONException
    {

        JSONObject veriler = new JSONObject(gelenler);

        String ss = veriler.getString("base64");
        int rows=veriler.getInt("rows");
        int cols=veriler.getInt("cols");
        System.out.println("Rows:"+rows+"Cols:"+cols);
        //ss=ss.replaceAll("\t","");
        String sonuc=calistir(ss,rows,cols);
        return sonuc;
    }

}