package tech.pucci.splitthis.enums;

public enum FacebookPermission {
    EMAIL("email"),
    PUBLIC_PROFILE("public_profile");

    public String value;

    FacebookPermission(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
