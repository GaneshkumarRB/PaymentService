package com.bnk.paymentservice.models;

import java.util.List;
import java.util.Objects;

public final class FraudScreeningResult {

    private final FraudDecisionType decision;
    private final int riskScore;
    private final List<String> reasons;

    public FraudScreeningResult(FraudDecisionType decision, int riskScore, List<String> reasons) {
        this.decision = decision;
        this.riskScore = riskScore;
        this.reasons = reasons;
    }

    public FraudDecisionType decision() {
        return decision;
    }

    public int riskScore() {
        return riskScore;
    }

    public List<String> reasons() {
        return reasons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FraudScreeningResult)) {
            return false;
        }
        FraudScreeningResult that = (FraudScreeningResult) o;
        return riskScore == that.riskScore
                && decision == that.decision
                && Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decision, riskScore, reasons);
    }

    @Override
    public String toString() {
        return "FraudScreeningResult{"
                + "decision=" + decision
                + ", riskScore=" + riskScore
                + ", reasons=" + reasons
                + '}';
    }
}
