package tpcreative.co.qrscanner.model;

import com.google.zxing.client.result.ParsedResultType;

import java.io.Serializable;

public class ItemNavigation implements Serializable {
    public int res;
    public EnumFragmentType enumFragmentType;
    public String value;
    public EnumAction enumAction;
    public ParsedResultType resultType;


    public ItemNavigation(ParsedResultType parsedResultType,EnumFragmentType enumFragmentType, EnumAction enumAction,int res, String value){
        this.enumFragmentType =enumFragmentType;
        this.enumAction = enumAction;
        this.value = value;
        this.res = res;
        this.resultType = parsedResultType;
    }

}
