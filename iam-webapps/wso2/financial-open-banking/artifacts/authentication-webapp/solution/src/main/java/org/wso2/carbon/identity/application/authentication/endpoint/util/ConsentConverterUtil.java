/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.identity.application.authentication.endpoint.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author amunasig
 */
public class ConsentConverterUtil {

    private static Map<String, String> consenMap = new HashMap<>();


    static {
        consenMap.put("ReadAccountsBasic", "Access account information.");
        consenMap.put("ReadAccountsDetail", "Access account identification details.");
        consenMap.put("ReadBalances", "Access to all account balance information.");
        consenMap.put("ReadBeneficiariesBasic", "Access beneficiary details.");
        consenMap.put("ReadBeneficiariesDetail", "Access account identification details for the beneficiary.");
        consenMap.put("ReadDirectDebits", "Access all Direct Debit information.");
        consenMap.put("ReadStandingOrdersBasic", "Access to standing order information.");
        consenMap.put("ReadStandingOrdersDetail", "Access account identification details for the beneficiary of standing orders.");
        consenMap.put("ReadTransactionsBasic", "Access to transaction information.");
        consenMap.put("ReadTransactionsDetail", "Access to transaction information.");
        consenMap.put("ReadTransactionsCredits", "Access to credit transaction information.");
        consenMap.put("ReadTransactionsDebits", "Access to debit transaction information.");
        consenMap.put("ReadStatementsBasic", "Access to basic statement information.");
        consenMap.put("ReadStatementsDetail", "Access to statement information.");
    }


    public static String getReadable(String str) {
        if (consenMap.containsKey(str)) {
            return consenMap.get(str);
        }
        else {
            return "";
        }
    }


}
