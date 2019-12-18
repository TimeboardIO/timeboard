package timeboard.core.api.sync;

public class ProjectSyncCredentialField {

    private String fieldKey;
    private String fieldName;
    private Type fieldType;
    private String value;
    private int weight;

    public ProjectSyncCredentialField(String fieldKey, String fieldName, Type fieldType, int weight) {
        this.fieldKey = fieldKey;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.weight = weight;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(Type fieldType) {
        this.fieldType = fieldType;
    }

    public enum Type{
        TEXT("text"),
        PASSWORD("password");

        private String name;

        private Type(String v) {
            this.name = v;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
