package jp.lg.ishinomaki.city.mrs.analyzer;

public class EmptyDataAnalyzer implements DataAnalyzer {

    public EmptyDataAnalyzer() {

    }
    
    @Override
    public void analyze(byte[] data) {
        System.out.println("EmptyDataAnalyzer.analyze");
    }

    @Override
    public byte[] getContents() {
        System.out.println("EmptyDataAnalyzer.getContents");
        return null;
    }

    @Override
    public String getDataType() {
        System.out.println("EmptyDataAnalyzer.getDataType");
        return null;
    }

}
