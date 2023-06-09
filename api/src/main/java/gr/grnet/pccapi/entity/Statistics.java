package gr.grnet.pccapi.entity;

public class Statistics {

  public String prefix;
  public int handlesCount;
  public int resolvableCount;
  public int unresolvableCount;
  public int uncheckedCount;

  public Statistics(
      String prefix,
      int handlesCount,
      int resolvableCount,
      int unresolvableCount,
      int uncheckedCount) {
    this.prefix = prefix;
    this.handlesCount = handlesCount;
    this.resolvableCount = resolvableCount;
    this.unresolvableCount = unresolvableCount;
    this.uncheckedCount = uncheckedCount;
  }
}
