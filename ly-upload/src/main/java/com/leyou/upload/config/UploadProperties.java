package com.leyou.upload.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "leyou.upload")
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadProperties {
    private String url;
    private List<String> allowType;

}
