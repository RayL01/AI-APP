package com.ray.aiapp.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for converting between float[] (Java) and vector (PostgreSQL pgvector).
 *
 * PostgreSQL pgvector stores vectors as a special 'vector' type.
 * We need to convert between Java's float[] and PostgreSQL's vector format.
 */
@Converter
public class VectorAttributeConverter implements AttributeConverter<float[], String> {

    /**
     * Convert float[] to PostgreSQL vector string format.
     * Example: [0.1, 0.2, 0.3] -> "[0.1,0.2,0.3]"
     *
     * Note: JDBC doesn't understand pgvector's 'vector' type directly.
     * We convert to String, and PostgreSQL implicitly casts it to vector.
     */
    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }

        // Java has no FloatStream, so we use a simple loop with StringBuilder
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(attribute[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convert PostgreSQL vector string to float[].
     * Example: "[0.1,0.2,0.3]" -> [0.1, 0.2, 0.3]
     */
    @Override
    public float[] convertToEntityAttribute(String dbData) {
        return parseVector(dbData);
    }

    /**
     * Static method to parse PostgreSQL vector string to float[].
     * Can be called directly without instantiating the converter.
     * Useful for Interface Projection results where @Convert doesn't apply.
     *
     * @param vectorString the vector string from database, e.g. "[0.1,0.2,0.3]"
     * @return float array representation of the vector
     */
    public static float[] parseVector(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return new float[0];
        }

        // Remove brackets and split by comma
        String cleaned = vectorString.replace("[", "").replace("]", "");
        if (cleaned.isEmpty()) {
            return new float[0];
        }

        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }

        return result;
    }

    /**
     * Static method to convert float[] to PostgreSQL vector string format.
     * Can be called directly without instantiating the converter.
     *
     * @param vector the float array to convert
     * @return vector string format, e.g. "[0.1,0.2,0.3]"
     */
    public static String toVectorString(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
