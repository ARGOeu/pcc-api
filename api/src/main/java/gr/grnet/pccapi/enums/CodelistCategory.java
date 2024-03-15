package gr.grnet.pccapi.enums;

public enum CodelistCategory {
  PROVIDER("provider"),
  LOOKUP_SERVICE("lookup_service"),
  CONTRACT_TYPE("contract_type");
  private final String text;

  CodelistCategory(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

  public String getText() {
    return text;
  }

  public static CodelistCategory getEnumByText(String code) {
    for (CodelistCategory e : CodelistCategory.values()) {
      if (e.text.equals(code)) {
        return e;
      }
    }
    return null;
  }
}
