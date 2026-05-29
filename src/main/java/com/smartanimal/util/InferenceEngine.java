package com.smartanimal.util;

import com.smartanimal.model.Symptom;
import com.smartanimal.model.HealthAssessment;

public class InferenceEngine {

    public static HealthAssessment generateAssessment(Symptom symptom) {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setSymptomId(symptom.getSymptomId());
        assessment.setAnimalId(symptom.getAnimalId());

        boolean hasAppetiteLoss = symptom.isReducedAppetite();
        boolean hasFever = symptom.isFever();
        boolean hasVomiting = symptom.isVomiting();
        boolean hasLowActivity = symptom.isLowActivity();
        boolean hasLimping = symptom.isLimping();

        String riskLevel = "Low";
        String condition = "Undetermined / Minor Fatigue";
        String action = "MONITOR: Keep a close eye on the animal for the next 24 hours. Ensure access to fresh water and rest. If symptoms persist or worsen, contact your vet.";

        // High Risk Rules
        if (hasVomiting && hasFever && hasAppetiteLoss) {
            riskLevel = "High";
            condition = "Parvovirus / Severe Infection";
            action = "EMERGENCY: Contact a veterinarian immediately! Isolate the animal from others. Do not administer any food or self-prescribed medications without professional instructions.";
        } 
        else if (hasVomiting && hasLowActivity && hasAppetiteLoss) {
            riskLevel = "High";
            condition = "Gastrointestinal Obstruction / Poisoning";
            action = "EMERGENCY: Immediate veterinary attention required. Ensure the animal does not ingest anything else. Bring any suspected materials or packaging if poisoning is suspected.";
        }
        // Medium Risk Rules
        else if (hasFever && hasAppetiteLoss && hasLowActivity) {
            riskLevel = "Medium";
            condition = "Systemic Viral or Bacterial Infection (e.g. Flu)";
            action = "VET VISIT: Schedule a vet checkup within 24 hours. Keep the animal in a warm, quiet place, monitor temperature, and encourage active hydration.";
        }
        else if (hasLimping && hasLowActivity) {
            riskLevel = "Medium";
            condition = "Musculoskeletal Injury / Joint Sprain";
            action = "VET VISIT: Restrict physical activity. Keep the animal on flat ground, preventing running or jumping. If limping persists for more than 24 hours, see a vet.";
        }
        else if (hasVomiting || hasFever) {
            riskLevel = "Medium";
            condition = hasVomiting ? "Acute Gastroenteritis / Dietary Indiscretion" : "Fever of Unknown Origin";
            action = "VET VISIT: If vomiting persists for more than 12 hours or fever spikes, consult a veterinarian. Withhold food for 12 hours but offer small frequent sips of water.";
        }
        // Low Risk Rules
        else if (hasAppetiteLoss || hasLowActivity || hasLimping) {
            riskLevel = "Low";
            if (hasLimping) {
                condition = "Mild Joint Stiffness / Muscle Soreness";
                action = "MONITOR: Check paw pads for cuts, thorns, or stings. Rest the animal and limit active play. If not resolved in 48 hours, seek professional care.";
            } else if (hasAppetiteLoss) {
                condition = "Temporary Appetite Suppression";
                action = "MONITOR: Ensure fresh water is available. Offer a bland diet (like boiled chicken and white rice) in small portions. If refusing food for 24h+, contact a vet.";
            } else {
                condition = "Mild Lethargy / Energy Depletion";
                action = "MONITOR: Allow the animal to rest in a comfortable environment. Observe behavior. If lethargy intensifies or other symptoms appear, schedule a vet checkup.";
            }
        }

        assessment.setRiskLevel(riskLevel);
        assessment.setPossibleCondition(condition);
        assessment.setRecommendedAction(action);
        return assessment;
    }
}
