package com.joutvhu.dynamic.jpa;

import com.joutvhu.dynamic.jpa.util.DynamicTemplateResolver;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Iterator;

/**
 * DynamicQueryTemplates
 *
 * @author Giao Ho
 * @see 1.0.0
 */
public class DynamicQueryTemplates implements ResourceLoaderAware, InitializingBean {
    private static Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    private static StringTemplateLoader sqlTemplateLoader = new StringTemplateLoader();

    static {
        cfg.setTemplateLoader(sqlTemplateLoader);
    }

    private DynamicTemplateResolver templateResolver = new DynamicTemplateResolver();

    private String encoding = "UTF-8";
    private String templateLocation = "classpath:/query";
    private String suffix = ".dsql";
    private ResourceLoader resourceLoader;

    public DynamicQueryTemplates() {
    }

    public Template findTemplate(String name) {
        try {
            return cfg.getTemplate(name, encoding);
        } catch (IOException e) {
            return null;
        }
    }

    public DynamicTemplateResolver getTemplateResolver() {
        templateResolver.setEncoding(encoding);
        return templateResolver;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String pattern;
        if (StringUtils.isNotBlank(templateLocation))
            pattern = templateLocation.contains(suffix) ? templateLocation : templateLocation + "/**/*" + suffix;
        else pattern = "classpath:/**/*" + suffix;

        PathMatchingResourcePatternResolver resourcePatternResolver =
                new PathMatchingResourcePatternResolver(resourceLoader);
        Resource[] resources = resourcePatternResolver.getResources(pattern);

        for (Resource resource : resources) {
            Iterator<Void> iterator = getTemplateResolver().doInTemplateResource(resource, (templateName, content) -> {
                Object src = sqlTemplateLoader.findTemplateSource(templateName);
//                if (src != null)
//                    logger.warn("found duplicate template key, will replace the value, key:" + templateName);
                sqlTemplateLoader.putTemplate(templateName, content);
            });
            while (iterator.hasNext()) {
                iterator.next();
            }
        }
    }
}