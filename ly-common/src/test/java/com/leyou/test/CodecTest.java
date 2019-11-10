package com.leyou.test;


import com.leyou.common.utils.CodecUtils;
import org.junit.jupiter.api.Test;

public class CodecTest {
    @Test
    public void test(){
        String shaHex = CodecUtils.shaHex("e21d44f200365b57fab2641cd31226d4", "05b0f203987e49d2b72b20b95e0e57d9");
        System.out.print(shaHex);
    }
}
