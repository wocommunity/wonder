// Applicant.java
// Created on Thu Dec 21 19:40:20  2000 by Apple EOModeler Version 410
package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class Applicant extends EOGenericRecord {

    /*
     * accessors: Double values
     */
    public Double earnedIncomeCredit() {
        return (Double) storedValueForKey("earnedIncomeCredit");
    }

    public Double earnedIncomeCreditAmount() {
        return (Double) storedValueForKey("earnedIncomeCreditAmount");
    }

    public Double income() {
        return (Double) storedValueForKey("income");
    }

    public Double incomeTax() {
        return (Double) storedValueForKey("incomeTax");
    }

    public Double incomeTaxWithheld() {
        return (Double) storedValueForKey("incomeTaxWithheld");
    }

    public Double taxFreeAllowance() {
        return (Double) storedValueForKey("taxFreeAllowance");
    }

    public Double taxableInterestIncome() {
        return (Double) storedValueForKey("taxableInterestIncome");
    }

    public Double unemploymentCompensation() {
        return (Double) storedValueForKey("unemploymentCompensation");
    }

    /*
     * custom acessors: businessLogic
     */
    public Double adjustedGrossIncome() {
        if (income() != null &&
            taxableInterestIncome() != null &&
            unemploymentCompensation() != null) {
            double income = income().doubleValue();
            double taxableInterestIncome = taxableInterestIncome().doubleValue();
            double unemploymentCompensation = unemploymentCompensation().doubleValue();

            return new Double(income + taxableInterestIncome + unemploymentCompensation);
        }
        else return null;
    }

    public Double taxableIncome() {
        if (taxFreeAllowance() != null) {
            double taxFreeAllowance = taxFreeAllowance().doubleValue();
            double adjustedGrossIncome = adjustedGrossIncome().doubleValue();

            if (adjustedGrossIncome > taxFreeAllowance)
                return new Double(adjustedGrossIncome - taxFreeAllowance);
            else return new Double(0);
        } else return null;
    }

    public Double totalPayments() {
        if (incomeTaxWithheld() != null && earnedIncomeCredit() != null) {
            double incomeTaxWithheld = incomeTaxWithheld().doubleValue();
            double earnedIncomeCredit = earnedIncomeCredit().doubleValue();

            return new Double(incomeTaxWithheld + earnedIncomeCredit);
        }
        else return null;
    }

    public Double refund() {
        if (incomeTax() != null) {
            double incomeTax = incomeTax().doubleValue();
            double totalPayments = totalPayments().doubleValue();

            if (totalPayments > incomeTax)
                return new Double(totalPayments - incomeTax);
            else return new Double(0);
        } else return null;
    }

    public Double amountYouOwe() {
        if (incomeTax() != null) {
            double incomeTax = incomeTax().doubleValue();
            double totalPayments = totalPayments().doubleValue();

            if (totalPayments < incomeTax)
                return new Double(incomeTax - totalPayments);
            else return new Double(0);
        } else return null;
    }
}