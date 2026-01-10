package com.ray.aiapp.service.dto;

/**
 * Created with IntelliJ IDEA.
 *用于动态代理 dto 获取相似度
 * @Author: shadyyyyyl
 * @Date: 2026/01/09/18:43
 * @Description:
 */
public interface chatWithScore {
    String getEmbeddingId();

    String getText();

    String getMetadata();

    String getEmbedding();

    Double getScore();
}