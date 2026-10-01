package com.smartanimal.util;

import com.smartanimal.model.VaccineRecommendation;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VaccineAdvisor {

    public static List<VaccineRecommendation> getRecommendations(String species, Integer age, String animalType) {
        return getRecommendations(species, age, animalType, null);
    }

    public static List<VaccineRecommendation> getRecommendations(String species, Integer age, String animalType, String breed) {
        List<VaccineRecommendation> list = new ArrayList<>();
        String sp = ((species != null ? species : "") + " " + (breed != null ? breed : "")).trim().toLowerCase();
        int safeAge = age != null ? age : 0;
        LocalDate today = LocalDate.now();

        // 1. Canine (Dog) Detection
        if (sp.contains("dog") || sp.contains("pup") || sp.contains("canine") || sp.contains("hound") || 
            sp.contains("retriever") || sp.contains("shepherd") || sp.contains("bulldog") || sp.contains("husky") || 
            sp.contains("poodle") || sp.contains("pug") || sp.contains("beagle") || sp.contains("labrador") || 
            sp.contains("boxer") || sp.contains("rottweiler") || sp.contains("chihuahua") || sp.contains("dalmatian") || 
            sp.contains("terrier") || sp.contains("mastiff") || sp.contains("corgi") || sp.contains("shihtzu") || 
            sp.contains("dachshund") || sp.contains("doberman")) {
            
            if (safeAge < 1) {
                // Puppy Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "DHPP Core (Distemper, Hepatitis, Parvovirus, Parainfluenza)",
                    "DHPP 1st Series",
                    "Puppy (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Initial Series (3-4 week booster intervals)",
                    "Primary core immunization protecting against life-threatening canine parvovirus and distemper.",
                    "Administer puppy 1st/2nd dose. Check deworming status."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Vaccine (Canine 1st Dose)",
                    "Rabies",
                    "Puppy (< 1 Year)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Mandatory 1st Dose (At 3-4 months)",
                    "Legally required immunization against fatal rabies virus.",
                    "Mandatory vaccination. Issue official rabies certificate."
                ));
                list.add(new VaccineRecommendation(
                    "Bordetella (Kennel Cough Protection)",
                    "Bordetella",
                    "Puppy (< 1 Year)",
                    "Recommended",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Every 6-12 Months",
                    "Protects against highly infectious tracheobronchitis, essential for social dogs, parks, and boarding.",
                    "Intranasal or subcutaneous formulation."
                ));
                list.add(new VaccineRecommendation(
                    "Leptospirosis 2-Dose Series",
                    "Leptospirosis",
                    "Puppy (< 1 Year)",
                    "Recommended",
                    28,
                    Date.valueOf(today.plusDays(28)),
                    "2-Dose Primary Series + Annual Booster",
                    "Bacterial defense against severe kidney and liver damage caused by contaminated water/soil.",
                    "Requires 2nd booster dose 3-4 weeks later."
                ));
                list.add(new VaccineRecommendation(
                    "Puppy Broad-Spectrum Deworming & Parasite Defense",
                    "Deworming Prophylaxis",
                    "Puppy (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Monthly Preventive",
                    "Essential elimination of internal intestinal worms (roundworms, hookworms) and heartworm prophylaxis.",
                    "Weigh animal accurately before dosing."
                ));
            } else if (safeAge <= 6) {
                // Adult Dog (1-6 years)
                list.add(new VaccineRecommendation(
                    "DHPP 7-in-1 / 9-in-1 Annual Booster",
                    "DHPP Booster",
                    "Adult (1 - 6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual or Triennial Booster",
                    "Maintains high protective antibody titers against Parvovirus, Distemper, and Adenovirus.",
                    "Routine annual booster shot."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Annual / Triennial Booster",
                    "Rabies Booster",
                    "Adult (1 - 6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Every 1-3 Years",
                    "Mandatory booster maintenance for legal rabies compliance and public safety.",
                    "Update veterinary passport / rabies tag."
                ));
                list.add(new VaccineRecommendation(
                    "Leptospirosis & Bordetella Annual Booster",
                    "Lepto + Bordetella",
                    "Adult (1 - 6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual Booster",
                    "Crucial yearly defense for outdoor, active, swimming, or socializing canines.",
                    "Administer during regular wellness checkup."
                ));
                list.add(new VaccineRecommendation(
                    "Heartworm & Tick-Borne Disease Screening",
                    "Vector Prophylaxis",
                    "Adult (1 - 6 Years)",
                    "Prophylaxis",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Annual Screening / Monthly Preventive",
                    "Blood screen (4Dx) and preventative against Lyme, Ehrlichia, Anaplasma, and Heartworm.",
                    "Combine with monthly oral/topical antiparasitic."
                ));
            } else {
                // Senior Dog (7+ years)
                list.add(new VaccineRecommendation(
                    "Rabies Senior Maintenance Booster",
                    "Rabies Senior",
                    "Senior (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Every 3 Years",
                    "Gentle core maintenance booster for senior and geriatric dogs.",
                    "Check health status and vitals prior to injection."
                ));
                list.add(new VaccineRecommendation(
                    "DHPP Core Antibody Titer & Booster",
                    "DHPP Senior",
                    "Senior (7+ Years)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Every 3 Years / Titer Based",
                    "Sustained viral immunity for senior dogs with lower stress protocol.",
                    "Evaluate blood titer or administer senior booster."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Geriatric Wellness & Joint Mobility Check",
                    "Geriatric Wellness",
                    "Senior (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual (Every 6 Months)",
                    "Comprehensive senior evaluation including renal, liver, cardiovascular, and arthritis assessment.",
                    "Recommend senior diet and joint mobility supplements."
                ));
            }
        }
        // 2. Feline (Cat) Detection
        else if (sp.contains("cat") || sp.contains("kitten") || sp.contains("feline") || sp.contains("persian") || 
                 sp.contains("siamese") || sp.contains("tabby") || sp.contains("maine") || sp.contains("ragdoll") || 
                 sp.contains("sphynx") || sp.contains("bengal") || sp.contains("shorthair") || sp.contains("burmese") || 
                 sp.contains("abyssinian") || sp.contains("savannah") || sp.contains("himalayan")) {
            
            if (safeAge < 1) {
                // Kitten Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "FVRCP Core (Feline Rhinotracheitis, Calicivirus, Panleukopenia)",
                    "FVRCP 1st Series",
                    "Kitten (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Series every 3-4 weeks until 16 weeks",
                    "Essential 3-in-1 core vaccine protecting kittens from upper respiratory virus and deadly distemper.",
                    "Administer 1st kitten dose subcutaneously in right front leg."
                ));
                list.add(new VaccineRecommendation(
                    "FeLV (Feline Leukemia Virus Vaccine)",
                    "FeLV 1st Series",
                    "Kitten (< 1 Year)",
                    "Recommended",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "2-Dose Primary Series",
                    "Protects against incurable retrovirus transmission, highly recommended for all kittens.",
                    "Test FeLV/FIV status before initial dose if possible."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Vaccine (Feline 1st Dose)",
                    "Rabies Feline",
                    "Kitten (< 1 Year)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "At 12-16 Weeks Old",
                    "Mandatory feline rabies immunization.",
                    "Administer in distal right rear leg per AAFP guidelines."
                ));
                list.add(new VaccineRecommendation(
                    "Kitten Deworming & Mite Prophylaxis",
                    "Deworming & Parasites",
                    "Kitten (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Monthly Preventive",
                    "Clears roundworms, hookworms, and ear mites common in young kittens.",
                    "Gentle pediatric dewormer suspension."
                ));
            } else if (safeAge <= 6) {
                // Adult Cat (1-6 years)
                list.add(new VaccineRecommendation(
                    "FVRCP Core Annual / Triennial Booster",
                    "FVRCP Booster",
                    "Adult (1 - 6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Every 1-3 Years",
                    "Maintains immunity against feline flu and panleukopenia virus.",
                    "Subcutaneous administration."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Feline Booster",
                    "Rabies Booster",
                    "Adult (1 - 6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual or Triennial",
                    "Legal and medical core protection against rabies.",
                    "Right rear leg administration."
                ));
                list.add(new VaccineRecommendation(
                    "FeLV Booster (Outdoor / Multi-Cat)",
                    "FeLV Booster",
                    "Adult (1 - 6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual (Outdoor cats)",
                    "Recommended for cats with outdoor access or contact with other cats.",
                    "Left rear leg administration."
                ));
            } else {
                // Senior Cat (7+ years)
                list.add(new VaccineRecommendation(
                    "FVRCP Senior Core Booster",
                    "FVRCP Senior",
                    "Senior (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Every 3 Years",
                    "Gentle booster protocol suited for aging feline immune systems.",
                    "Assess general health and kidney function."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Maintenance Booster",
                    "Rabies Senior",
                    "Senior (7+ Years)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Every 3 Years",
                    "Rabies antibody maintenance booster.",
                    "AAFP distal leg protocol."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Feline Renal & Thyroid Health Profile",
                    "Geriatric Profile",
                    "Senior (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual (Every 6 Months)",
                    "Early detection of chronic kidney disease (CKD), hyperthyroidism, and dental health.",
                    "Comprehensive blood chemistry & urinalysis recommended."
                ));
            }
        }
        // 3. Bovine (Cattle) Detection
        else if (sp.contains("cow") || sp.contains("cattle") || sp.contains("bovine") || sp.contains("calf") || 
                 sp.contains("buffalo") || sp.contains("bull") || sp.contains("ox")) {
            
            if (safeAge < 1) {
                // Calf Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "FMD (Foot and Mouth Disease) 1st Dose",
                    "FMD Calf",
                    "Calf (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Primary Dose at 3-4 Months + Bi-annual",
                    "Critical defense against contagious aphthovirus causing severe hoof and oral blisters.",
                    "Subcutaneous deep injection."
                ));
                list.add(new VaccineRecommendation(
                    "Clostridial 7-Way / 8-Way (Blackleg & Malignant Edema)",
                    "Blackleg 7-Way",
                    "Calf (< 1 Year)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "At 3-6 Months + Booster in 4 weeks",
                    "Prevents acute fatal muscular and tissue clostridial toxemia in young calves.",
                    "Inject subcutaneously in neck region."
                ));
                list.add(new VaccineRecommendation(
                    "Brucellosis (Strain 19 / RB51 Heifer Vaccine)",
                    "Brucellosis",
                    "Calf (< 1 Year)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Single Lifetime Dose (4-8 Months)",
                    "Vital official vaccination for female heifer calves to prevent brucellosis abortion.",
                    "Only certified veterinarians may administer and ear-tag."
                ));
                list.add(new VaccineRecommendation(
                    "HS (Hemorrhagic Septicemia) & BQ Vaccine",
                    "HS + BQ",
                    "Calf (< 1 Year)",
                    "Core",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Primary Dose at 6 Months",
                    "Shields against acute Pasteurella multocida septicemia, particularly pre-monsoon.",
                    "Essential for livestock survival in tropical climates."
                ));
                list.add(new VaccineRecommendation(
                    "Broad-Spectrum Deworming Prophylaxis",
                    "Calf Deworming",
                    "Calf (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Quarterly Preventive",
                    "Clears internal parasites in young growing calves to improve weight gain.",
                    "Dose based on body weight."
                ));
            } else if (safeAge <= 6) {
                // Adult Cattle (1-6 years)
                list.add(new VaccineRecommendation(
                    "FMD (Foot and Mouth Disease) Bi-Annual Booster",
                    "FMD Booster",
                    "Adult Cattle (1-6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Bi-Annual (Every 6 Months)",
                    "Mandatory periodic booster to sustain herd-wide immunity and milk production.",
                    "Administer to entire herd simultaneously."
                ));
                list.add(new VaccineRecommendation(
                    "Hemorrhagic Septicemia (HS) Pre-Monsoon Booster",
                    "HS Booster",
                    "Adult Cattle (1-6 Years)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Annual (May-June Pre-Monsoon)",
                    "Seasonal booster to safeguard livestock against wet weather bacterial outbreaks.",
                    "Administer prior to onset of rainy season."
                ));
                list.add(new VaccineRecommendation(
                    "Black Quarter (BQ) & Anthrax Booster",
                    "BQ / Anthrax",
                    "Adult Cattle (1-6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual Booster",
                    "Protection against soil-borne Clostridium and Bacillus spores in grazing pastures.",
                    "Recommended for grazing and pasture herds."
                ));
                list.add(new VaccineRecommendation(
                    "Theileriosis (Tick Fever) Live Attenuated Vaccine",
                    "Theileriosis",
                    "Adult Cattle (1-6 Years)",
                    "Recommended",
                    45,
                    Date.valueOf(today.plusDays(45)),
                    "One-time / Annual for Crossbreds",
                    "Crucial tick-borne protozoal defense for high-yielding and exotic cattle breeds.",
                    "Check animal temperature before vaccination."
                ));
            } else {
                // Senior Cattle (7+ years)
                list.add(new VaccineRecommendation(
                    "FMD Senior Ruminant Booster",
                    "FMD Senior",
                    "Senior Cattle (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Bi-Annual (Every 6 Months)",
                    "Maintains FMD antibody levels in aging livestock with minimal stress.",
                    "Observe herd post-injection for reactions."
                ));
                list.add(new VaccineRecommendation(
                    "Brucellosis & TB Diagnostic Screening",
                    "Bovine Screening",
                    "Senior Cattle (7+ Years)",
                    "Core",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual Diagnostic",
                    "Routine diagnostic screening for herd safety, public health, and milk quality.",
                    "Mandatory tuberculosis and brucellosis screening."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Cattle Wellness & Nutrition Assessment",
                    "Geriatric Wellness",
                    "Senior Cattle (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Wellness Review",
                    "Evaluation of dental wear, rumen health, body condition score, and mineral supplementation.",
                    "Review feed quality and provide mineral block."
                ));
            }
        }
        // 4. Caprine/Ovine (Goat/Sheep) Detection
        else if (sp.contains("goat") || sp.contains("sheep") || sp.contains("lamb") || sp.contains("kid") || 
                 sp.contains("caprine") || sp.contains("ovine") || sp.contains("ram") || sp.contains("ewe")) {
            
            if (safeAge < 1) {
                // Lamb / Kid Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Enterotoxemia (CD&T - Pulpy Kidney & Tetanus)",
                    "CD&T Primary",
                    "Lamb / Kid (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "At 4-8 Weeks + Booster at 12 Weeks",
                    "Prevents fatal overeating disease and tetanus in growing young ruminants.",
                    "Subcutaneous injection in axilla/neck."
                ));
                list.add(new VaccineRecommendation(
                    "PPR (Peste des Petits Ruminants / Goat Plague)",
                    "PPR Vaccine",
                    "Lamb / Kid (< 1 Year)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "At 3-4 Months Old",
                    "Critical defense against severe viral pneumo-enteritis in sheep and goats.",
                    "Confers 3-year robust immunity."
                ));
                list.add(new VaccineRecommendation(
                    "Goat Pox / Sheep Pox Vaccine",
                    "Goat / Sheep Pox",
                    "Lamb / Kid (< 1 Year)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "At 3-5 Months Old",
                    "Protects against poxvirus nodular lesions and fever outbreaks.",
                    "Intradermal or subcutaneous depending on formulation."
                ));
                list.add(new VaccineRecommendation(
                    "Intestinal Deworming & Coccidiosis Control",
                    "Lamb/Kid Dewormer",
                    "Lamb / Kid (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Monthly Preventive",
                    "Treats internal roundworms and coccidia parasites common in young kids/lambs.",
                    "Oral drench administration."
                ));
            } else if (safeAge <= 6) {
                // Adult Sheep / Goat (1-6 years)
                list.add(new VaccineRecommendation(
                    "CD&T (Enterotoxemia & Tetanus) Annual Booster",
                    "CD&T Booster",
                    "Adult Sheep/Goat (1-6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster (Pre-kidding/lambing)",
                    "Protects does/ewes and provides maternal colostrum antibodies for newborn kids.",
                    "Administer 4-6 weeks before expected kidding."
                ));
                list.add(new VaccineRecommendation(
                    "PPR Triennial Booster",
                    "PPR Booster",
                    "Adult Sheep/Goat (1-6 Years)",
                    "Core",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Every 3 Years",
                    "Maintains small ruminant flock immunity against PPR viral epidemics.",
                    "Subcutaneous injection."
                ));
                list.add(new VaccineRecommendation(
                    "HS / FMD Seasonal Small Ruminant Booster",
                    "HS / FMD Ruminant",
                    "Adult Sheep/Goat (1-6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual / Bi-annual",
                    "Protects herd during seasonal climate transitions and grazing migration.",
                    "Deep subcutaneous injection."
                ));
            } else {
                // Senior Sheep / Goat (7+ years)
                list.add(new VaccineRecommendation(
                    "CD&T Senior Maintenance Booster",
                    "CD&T Senior",
                    "Senior Sheep/Goat (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Tetanus and enterotoxemia protection for aging sheep and goats.",
                    "Gentle injection technique."
                ));
                list.add(new VaccineRecommendation(
                    "Geriatric Small Ruminant Joint & Parasite Wellness",
                    "Geriatric Wellness",
                    "Senior Sheep/Goat (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Examination",
                    "Wellness checks for arthritis, dental health, and FAMACHA score for anemia/parasite load.",
                    "Recommend easy-to-chew diet and mineral block."
                ));
            }
        }
        // 5. Equine (Horse) Detection
        else if (sp.contains("horse") || sp.contains("equine") || sp.contains("foal") || sp.contains("pony") || 
                 sp.contains("mare") || sp.contains("stallion")) {
            
            if (safeAge < 1) {
                // Foal Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Equine Core 4-Way (Tetanus, EEE, WEE, WNV)",
                    "Core 4-Way Foal",
                    "Foal (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "3-Dose Series starting at 4-6 Months",
                    "Comprehensive encephalomyelitis, tetanus toxoid, and West Nile Virus protection.",
                    "Intramuscular injection in neck triangle."
                ));
                list.add(new VaccineRecommendation(
                    "Equine Rabies Vaccine (1st Dose)",
                    "Rabies Equine",
                    "Foal (< 1 Year)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "At 6 Months Old",
                    "Mandatory single-dose rabies immunization for foals.",
                    "Administer intramuscularly."
                ));
                list.add(new VaccineRecommendation(
                    "Foal Broad-Spectrum Deworming Prophylaxis",
                    "Foal Dewormer",
                    "Foal (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Bi-Monthly course",
                    "Essential deworming against ascarids (roundworms) in young foals.",
                    "Weigh or use weight tape for dosage."
                ));
            } else if (safeAge <= 6) {
                // Adult Horse (1-6 years)
                list.add(new VaccineRecommendation(
                    "Equine Core 5-Way Annual Booster (Tetanus, EEE, WEE, WNV, Rabies)",
                    "Equine 5-Way Booster",
                    "Adult Horse (1-6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Spring Booster",
                    "Full equine core booster protecting against mosquito vectors, tetanus, and rabies.",
                    "Administer before peak vector mosquito season in spring."
                ));
                list.add(new VaccineRecommendation(
                    "Equine Influenza & EHV-1/4 (Rhinopneumonitis)",
                    "Flu / Rhino Booster",
                    "Adult Horse (1-6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Every 6 Months (Active horses)",
                    "Essential respiratory protection for performance, boarding, racing, and traveling equines.",
                    "Required by most equestrian venues."
                ));
            } else {
                // Senior Horse (7+ years)
                list.add(new VaccineRecommendation(
                    "Equine Core 5-Way Senior Booster",
                    "Equine 5-Way Senior",
                    "Senior Horse (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Spring Booster",
                    "Core mosquito-borne vector protection, tetanus, and rabies for senior horses.",
                    "Routine spring booster injection."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Equine Cushing's & Geriatric Metabolic Screening",
                    "Geriatric Wellness",
                    "Senior Horse (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Annual Metabolic Screening",
                    "Screening for Pituitary Pars Intermedia Dysfunction (PPID/Cushing's), dental health, and arthritis.",
                    "Schedule diagnostic blood draws and dental floating."
                ));
            }
        }
        // 6. Leporidae (Rabbit) Detection
        else if (sp.contains("rabbit") || sp.contains("bunny") || sp.contains("hare")) {
            if (safeAge < 1) {
                // Kit Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Myxomatosis + RHDV1 / RHDV2 Triple Combination Vaccine",
                    "Myxo-RHD Plus",
                    "Kit (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "At 5-8 Weeks Old",
                    "Protects pet rabbits from fatal Myxomatosis and Rabbit Haemorrhagic Disease strains 1 & 2.",
                    "Subcutaneous injection. Primary dose."
                ));
                list.add(new VaccineRecommendation(
                    "Pediatric Deworming & Parasite Check",
                    "Kit Dewormer",
                    "Kit (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "One-time check",
                    "Prophylaxis against internal and external parasites in young rabbits.",
                    "Dose based on exact weight."
                ));
            } else if (safeAge <= 6) {
                // Adult Rabbit (1-6 years)
                list.add(new VaccineRecommendation(
                    "Myxomatosis & RHDV1/2 Annual Booster",
                    "Myxo-RHD Booster",
                    "Adult Rabbit (1-6 Years)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Annual Booster",
                    "Annual booster to sustain immunity against fatal rabbit hemorrhagic disease and myxomatosis.",
                    "Routine yearly injection."
                ));
                list.add(new VaccineRecommendation(
                    "Rabbit Deworming (Fenbendazole / E. cuniculi Prophylaxis)",
                    "E. cuniculi Defense",
                    "Adult Rabbit (1-6 Years)",
                    "Prophylaxis",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Bi-Annual Course",
                    "Prevents Encephalitozoon cuniculi neurological and renal parasite infection.",
                    "Oral paste protocol under vet supervision."
                ));
            } else {
                // Senior Rabbit (7+ years)
                list.add(new VaccineRecommendation(
                    "Myxomatosis & RHDV1/2 Senior Booster",
                    "Myxo-RHD Senior",
                    "Senior Rabbit (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Gentle booster protection tailored for senior rabbits.",
                    "Assess body condition score before injection."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Rabbit Dental & Kidney Wellness Exam",
                    "Geriatric Wellness",
                    "Senior Rabbit (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Wellness Review",
                    "Crucial screening for senior rabbits to detect dental spurs, arthritis, and chronic renal issues.",
                    "Ensure proper fibrous fiber/hay diet."
                ));
            }
        }
        // 7. Avian (Bird/Poultry) Detection
        else if (sp.contains("bird") || sp.contains("chicken") || sp.contains("poultry") || sp.contains("hen") || 
                 sp.contains("rooster") || sp.contains("duck") || sp.contains("turkey") || sp.contains("avian")) {
            if (safeAge < 1) {
                // Chick Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Newcastle Disease (ND - LaSota / B1) + Infectious Bronchitis (IB)",
                    "ND + IB Primary",
                    "Chick (< 1 Year)",
                    "Core",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Day 7, Day 21 + Every 3 Months",
                    "Protects against devastating Ranikhet (Newcastle) respiratory and neurological disease.",
                    "Ocular drop or drinking water application."
                ));
                list.add(new VaccineRecommendation(
                    "Infectious Bursal Disease (Gumboro) Vaccine",
                    "Gumboro (IBD)",
                    "Chick (< 1 Year)",
                    "Core",
                    10,
                    Date.valueOf(today.plusDays(10)),
                    "At 14-21 Days Old",
                    "Prevents severe immunosuppressive viral disease in young poultry.",
                    "Administer via cool chlorine-free water."
                ));
                list.add(new VaccineRecommendation(
                    "Fowl Pox Vaccine",
                    "Fowl Pox",
                    "Chick (< 1 Year)",
                    "Recommended",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "At 6-8 Weeks Old",
                    "Protects against cutaneous and respiratory fowl pox lesions.",
                    "Wing-web stab method."
                ));
            } else if (safeAge <= 6) {
                // Adult Poultry (1-6 years)
                list.add(new VaccineRecommendation(
                    "Newcastle Disease & IB Quarterly Booster",
                    "ND + IB Booster",
                    "Adult Poultry (1-6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Quarterly Booster",
                    "Quarterly booster maintenance to sustain flock-wide viral immunity.",
                    "Administer via drinking water to the entire flock."
                ));
                list.add(new VaccineRecommendation(
                    "Fowl Pox Annual Booster",
                    "Fowl Pox Booster",
                    "Adult Poultry (1-6 Years)",
                    "Recommended",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Annual Booster",
                    "Maintains protection against seasonal fowl pox outbreaks.",
                    "Wing-web puncture method."
                ));
            } else {
                // Senior Poultry (7+ years)
                list.add(new VaccineRecommendation(
                    "Avian Senior Wellness & Parasite Check",
                    "Geriatric Wellness",
                    "Senior Poultry (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Wellness Review",
                    "Physical check for senior birds including feather quality, mite load, and joint health.",
                    "Evaluate egg laying safety and diet."
                ));
            }
        }
        // 8. Porcine (Pig) Detection
        else if (sp.contains("pig") || sp.contains("swine") || sp.contains("porcine") || sp.contains("piglet") || 
                 sp.contains("hog")) {
            if (safeAge < 1) {
                // Piglet Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Swine Fever (Classical Swine Fever - CSF)",
                    "CSF Vaccine",
                    "Piglet (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "At 6-8 Weeks Old",
                    "Crucial protection against highly contagious and fatal Hog Cholera / Classical Swine Fever.",
                    "Intramuscular injection behind ear base."
                ));
                list.add(new VaccineRecommendation(
                    "Porcine Parvovirus & Erysipelas (PPV + Erysipelas)",
                    "PPV + Erysipelas Primary",
                    "Piglet (< 1 Year)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Primary series",
                    "Prevents diamond skin disease, arthritis, and reproductive failure in pigs.",
                    "Intramuscular injection."
                ));
                list.add(new VaccineRecommendation(
                    "Piglet Broad-Spectrum Deworming",
                    "Piglet Dewormer",
                    "Piglet (< 1 Year)",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "One-time check",
                    "Clears internal parasites in young growing piglets.",
                    "Dose based on weight."
                ));
            } else if (safeAge <= 6) {
                // Adult Pig (1-6 years)
                list.add(new VaccineRecommendation(
                    "Swine Fever (CSF) Annual Booster",
                    "CSF Booster",
                    "Adult Pig (1-6 Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Annual booster to maintain herd protection against classical swine fever virus.",
                    "Intramuscular injection."
                ));
                list.add(new VaccineRecommendation(
                    "PPV + Erysipelas Bi-Annual Booster",
                    "PPV + Erysipelas Booster",
                    "Adult Pig (1-6 Years)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Pre-breeding + Bi-annual",
                    "Maintains high antibody levels to prevent diamond skin disease and reproductive loss.",
                    "Intramuscular injection."
                ));
            } else {
                // Senior Pig (7+ years)
                list.add(new VaccineRecommendation(
                    "Swine Fever Senior Booster",
                    "CSF Senior",
                    "Senior Pig (7+ Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Swine fever booster for senior/geriatric breeding stock.",
                    "Gentle injection."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Swine Geriatric Wellness & Joint Check",
                    "Geriatric Wellness",
                    "Senior Pig (7+ Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Wellness Review",
                    "Screening for osteoarthritis, hoof health, and general body condition in senior swine.",
                    "Ensure proper bedding and hoof trimming."
                ));
            }
        }
        // 9. General / Other Animals
        else {
            if (safeAge < 1) {
                // Pediatric (< 1 year)
                list.add(new VaccineRecommendation(
                    "Core Species Prophylactic Immunization",
                    "Core Prophylaxis",
                    "Juvenile (< 1 Year)",
                    "Core",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Primary / Annual Booster",
                    "Standard veterinary baseline immunization tailored to species class and biological age.",
                    "Conduct full physical exam before vaccination."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Prophylaxis (Mammalian Species)",
                    "Rabies Prophylaxis",
                    "Juvenile (< 1 Year)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual / Triennial",
                    "Standard rabies protection for domestic and farm mammals.",
                    "Official veterinary compliance record."
                ));
                list.add(new VaccineRecommendation(
                    "Broad-Spectrum Parasite & Deworming Prophylaxis",
                    "Parasite Defense",
                    "All Ages",
                    "Prophylaxis",
                    3,
                    Date.valueOf(today.plusDays(3)),
                    "Seasonal / Quarterly",
                    "Complete protection against internal parasites and seasonal external vectors.",
                    "Dosage calibrated by weight."
                ));
            } else if (safeAge <= 6) {
                // Adult (1-6 years)
                list.add(new VaccineRecommendation(
                    "Core Species Annual Booster",
                    "Core Booster",
                    "Adult (" + safeAge + " Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Routine annual booster immunization tailored to species.",
                    "Check overall health before administration."
                ));
                list.add(new VaccineRecommendation(
                    "Rabies Annual Booster (Mammalian)",
                    "Rabies Booster",
                    "Adult (" + safeAge + " Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Mandatory rabies vaccine maintenance for adult mammals.",
                    "Record in health pass."
                ));
                list.add(new VaccineRecommendation(
                    "Adult Parasite Prevention",
                    "Dewormer Booster",
                    "Adult (" + safeAge + " Years)",
                    "Prophylaxis",
                    30,
                    Date.valueOf(today.plusDays(30)),
                    "Seasonal Preventive",
                    "Routine seasonal deworming and vector control.",
                    "Administer during annual wellness exam."
                ));
            } else {
                // Senior (7+ years)
                list.add(new VaccineRecommendation(
                    "Core Species Senior Maintenance Booster",
                    "Core Senior",
                    "Senior (" + safeAge + " Years)",
                    "Core",
                    14,
                    Date.valueOf(today.plusDays(14)),
                    "Annual Booster",
                    "Tailored core booster protocol for older domestic/farm animals.",
                    "Aimed at preserving immunity with minimum stress."
                ));
                list.add(new VaccineRecommendation(
                    "Senior Rabies Maintenance Booster",
                    "Rabies Senior",
                    "Senior (" + safeAge + " Years)",
                    "Core",
                    21,
                    Date.valueOf(today.plusDays(21)),
                    "Every 3 Years",
                    "Rabies booster compliance for senior animals.",
                    "Verify health status."
                ));
                list.add(new VaccineRecommendation(
                    "Geriatric Wellness Profile & Senior Check",
                    "Geriatric Wellness",
                    "Senior (" + safeAge + " Years)",
                    "Wellness",
                    7,
                    Date.valueOf(today.plusDays(7)),
                    "Bi-Annual Wellness Review",
                    "Comprehensive senior assessment focusing on organs, joints, and dental wear.",
                    "Review daily activity and food intake."
                ));
            }
        }

        return list;
    }
}
