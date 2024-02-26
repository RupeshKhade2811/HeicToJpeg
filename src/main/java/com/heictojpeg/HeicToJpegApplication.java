package com.heictojpeg;


import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Stream2BufferedImage;
import org.im4java.process.OutputConsumer;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class HeicToJpegApplication {
	private static final String IMAGE_MAGICK_PATH = "C:\\Program Files\\ImageMagick-7.1.1-Q16-HDRI";

	public static void main(String[] args) throws IOException, InterruptedException, IM4JavaException {
		SpringApplication.run(HeicToJpegApplication.class, args);

		ConvertCmd cmd = new ConvertCmd();
		cmd.setSearchPath(IMAGE_MAGICK_PATH);

			/*IMOperation op = new IMOperation();
			op.addImage("C:\\myimage\\IMG_1646.HEIC");
			op.addImage("C:\\myimage\\sample1.jpeg");
			try {
				cmd.run(op);
			} catch (IOException | InterruptedException | IM4JavaException e) {
				e.printStackTrace();
			}*/


	}


	@GetMapping(value = "/getheictojpegbyte", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getJpegByte() throws IOException, InterruptedException, IM4JavaException {
		byte[] heicBytes = Files.readAllBytes(Paths.get("C:\\myimage\\IMG_1642.HEIC"));
		byte[] jpegBytes = convertHEICtoJPEG(heicBytes);
		return jpegBytes;
	}
	public static byte[] convertHEICtoJPEG(byte[] heicBytes) throws IOException, InterruptedException, IM4JavaException {
		IMOperation op = new IMOperation();
		op.addImage("-");
		op.addImage("jpeg:-");

// set up pipe(s): you can use one or two pipe objects
		//you can covert byte[] to temp file and add to FileInputStream
		File tempFile = File.createTempFile("temp", ".heic");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			fos.write(heicBytes);
		}
		FileInputStream fis = new FileInputStream(tempFile);

// Pipe pipe = new Pipe(fis,fos);
		Pipe pipeIn  = new Pipe(fis,null);
		//Pipe pipeOut = new Pipe(null,fos);

// set up command
		ConvertCmd convert = new ConvertCmd();
		convert.setSearchPath(IMAGE_MAGICK_PATH);//This is important to set the path of ImageMagick tool
		convert.setInputProvider(pipeIn); //seting input by using pipe
		Stream2BufferedImage s2b = new Stream2BufferedImage();
		convert.setOutputConsumer(s2b);//setting output using Stream2BufferedImage
		convert.run(op);
		BufferedImage image = s2b.getImage(); //getting bufferImage
		byte[] imageBytes = imageToBytes(image);//converting bufferImage to byte[]
		fis.close();
		return imageBytes;

	}

	private static byte[] imageToBytes(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpeg", baos);
		baos.flush();
		byte[] imageBytes = baos.toByteArray();
		baos.close();
		return imageBytes;
	}


}
