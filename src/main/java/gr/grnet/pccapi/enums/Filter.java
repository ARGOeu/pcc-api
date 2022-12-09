package gr.grnet.pccapi.enums;

public enum Filter {
  URL("URL"),
  EMAIL("EMAIL"),
  CHECKSUM("CHECKSUM"),
  RETRIEVE_RECORDS("retrieverecords");

  private final String text;

  Filter(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
