

public class RegCompModel {
  private int rowIndex;
  private int[] attrXs;
  private int attrY;
  private double[] beta;

  public RegCompModel(int rowIndex, int[] attrXs, int attrY) {
    setRowIndex(rowIndex);
    setAttrXs(attrXs);
    setAttrY(attrY);
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public void setRowIndex(int rowIndex) {
    this.rowIndex = rowIndex;
  }

  public int[] getAttrXs() {
    return attrXs;
  }

  public void setAttrXs(int[] attrXs) {
    this.attrXs = attrXs;
  }

  public int getAttrY() {
    return attrY;
  }

  public void setAttrY(int attrY) {
    this.attrY = attrY;
  }

  public double[] getBeta() {
    return beta;
  }

  public void setBeta(double[] beta) {
    this.beta = beta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    for (int attrX : attrXs) {
      result = prime * result + attrX;
    }
    result = prime * result + attrY;
    result = prime * result + rowIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RegCompModel other = (RegCompModel) obj;
    if (attrY != other.getAttrY()) {
      return false;
    }
    if (attrXs.length != other.getAttrXs().length) {
      return false;
    }

    for (int i = 0; i < attrXs.length; ++i) {
      if (attrXs[i] != other.getAttrXs()[i]) {
        return false;
      }
    }
    if (rowIndex != other.getRowIndex()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("[");
    for (int attrX : attrXs) {
      sb.append(attrX + ",");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("]");
    sb.append("->");
    sb.append(attrY);

    sb.append("coreRowIndex:" + rowIndex + " ");

    return sb.toString();
  }

}
