package com.leyou.upload.service;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.upload.config.UploadProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    private final static Logger logger=LoggerFactory.getLogger(UploadProperties.class);
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties uploadProperties;
    public String upload(MultipartFile file) {
        //检验文件
          //（1 检验文件后缀名
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if(!uploadProperties.getAllowType().contains(contentType)){
             //文件不合法
            logger.error("文件内容不合法:{}",filename);
        }
        try {
          //(2  检验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image==null){
                logger.error("文件内容不合法{}",filename);
               throw new LyException(ExceptionEnum.FILE_UPLOAD_FAIL);
            }
        //保存图片
        String ext=StringUtils.substringAfterLast(filename,".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            //返回图片url
            return uploadProperties.getUrl()+storePath.getFullPath();
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_FAIL);
        }
    }
}
