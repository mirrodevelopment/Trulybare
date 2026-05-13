package com.trulybare.config;

import com.trulybare.entity.Category;
import com.trulybare.entity.Product;
import com.trulybare.repository.CategoryRepository;
import com.trulybare.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSeeder: Responsible for initializing the database with the new Truly Bare product system.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.trulybare.repository.UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        seedDatabase();
        seedAdminUser();
        seedSlides();
    }



    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private void seedAdminUser() {
        try {
            // Primary Admin
            com.trulybare.entity.User admin = userRepository.findByEmail("Trulybare@admin").orElse(new com.trulybare.entity.User());
            admin.setFullName("Truly Bare Admin");
            admin.setEmail("Trulybare@admin");
            if (admin.getPhone() == null || admin.getPhone().equals("9344075887")) admin.setPhone("0000000000");
            admin.setPassword(passwordEncoder.encode("truly@bare123"));
            admin.setRole(com.trulybare.enums.UserRole.ADMIN);
            admin.setTermsAccepted(true);
            admin.setIsActive(true);
            admin.setCustomerStatus("Platinum");
            userRepository.save(admin);
            System.out.println("✅ Primary admin seeded: Trulybare@admin");

            // Alternative Admin
            com.trulybare.entity.User altAdmin = userRepository.findByEmail("admin@trulybare.com").orElse(new com.trulybare.entity.User());
            altAdmin.setFullName("Alternative Admin");
            altAdmin.setEmail("admin@trulybare.com");
            if (altAdmin.getPhone() == null) altAdmin.setPhone("1111111111");
            altAdmin.setPassword(passwordEncoder.encode("Admin@123"));
            altAdmin.setRole(com.trulybare.enums.UserRole.ADMIN);
            altAdmin.setTermsAccepted(true);
            altAdmin.setIsActive(true);
            altAdmin.setCustomerStatus("Gold");
            userRepository.save(altAdmin);
            System.out.println("✅ Alt admin seeded: admin@trulybare.com");
            
            // Sivasurya Admin
            com.trulybare.entity.User sivaAdmin = userRepository.findByEmail("sivasurya.official.8@gmail.com").orElse(new com.trulybare.entity.User());
            sivaAdmin.setFullName("SIVASURYA S");
            sivaAdmin.setEmail("sivasurya.official.8@gmail.com");
            if (sivaAdmin.getPhone() == null) sivaAdmin.setPhone("9344075887");
            sivaAdmin.setPassword("$2a$10$aygqtdXJdekVNdq4cZLhquT7.dA8UV/IQemN7Ru2WsfRcuNmlt1Ru");
            sivaAdmin.setRole(com.trulybare.enums.UserRole.ADMIN);
            sivaAdmin.setTermsAccepted(true);
            sivaAdmin.setIsActive(true);
            sivaAdmin.setCustomerStatus("Platinum");
            userRepository.save(sivaAdmin);
            System.out.println("✅ Sivasurya admin seeded: sivasurya.official.8@gmail.com");

            // User Requested Admin
            com.trulybare.entity.User reqAdmin = userRepository.findByEmail("admin").orElse(new com.trulybare.entity.User());
            reqAdmin.setFullName("Requested Admin");
            reqAdmin.setEmail("admin");
            if (reqAdmin.getPhone() == null) reqAdmin.setPhone("9999999999");
            reqAdmin.setPassword(passwordEncoder.encode("pass123"));
            reqAdmin.setRole(com.trulybare.enums.UserRole.ADMIN);
            reqAdmin.setTermsAccepted(true);
            reqAdmin.setIsActive(true);
            reqAdmin.setCustomerStatus("Platinum");
            userRepository.save(reqAdmin);
            System.out.println("✅ Requested admin seeded: admin");
            
            System.out.println("✅ All admin accounts seeded successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error seeding admin users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Autowired
    private com.trulybare.repository.SlideRepository slideRepository;

    private void seedSlides() {
        if (slideRepository.count() == 0) {
            slideRepository.save(new com.trulybare.entity.Slide(null, "Pure Botanical Skincare", "Rituals rooted in nature. Results backed by science.", "/assets/images/skin care 1 .jpeg", "Shop Skin Care", "/products/category/2", true, 1, null, null, null));
            slideRepository.save(new com.trulybare.entity.Slide(null, "Revitalize Your Hair", "Nourish every strand with our hair care collection.", "/assets/images/haircre 2 .jpeg", "Shop Hair Care", "/products/category/3", true, 2, null, null, null));
            slideRepository.save(new com.trulybare.entity.Slide(null, "Pain Relief Formulas", "Ancient wisdom, modern relief.", "/assets/images/sanjeevi pain realif .jpeg", "Explore Now", "/products", true, 3, null, null, null));
            System.out.println("✅ Sample homepage banners created");
        }
    }

    private void seedDatabase() {
        // ── Categories ──────────────────────────────────────────────────────────
        Category vegam     = save(createCategoryIfMissing("HOUSE OF VEGAM", null));
        Category pain      = save(createCategoryIfMissing("PAIN RELIEF",    vegam));
        Category cosmetics = save(createCategoryIfMissing("COSMETICS",      null));
        Category supps     = save(createCategoryIfMissing("SUPPLEMENTS",    cosmetics));
        Category weight    = save(createCategoryIfMissing("WEIGHT LOSS",    cosmetics));
        Category skin      = save(createCategoryIfMissing("SKIN CARE",      cosmetics));
        Category hair      = save(createCategoryIfMissing("HAIR CARE",      cosmetics));

        vegam.setDescription("Traditional Ayurvedic and clinical formulations from the House of Vegam Pain Relief Clinic."); categoryRepository.save(vegam);
        pain.setDescription("Ancient wisdom meeting modern relief for chronic and acute pain management.");                   categoryRepository.save(pain);
        cosmetics.setDescription("A curated archive of extraordinary botanical rituals for those who seek the exceptional."); categoryRepository.save(cosmetics);
        supps.setDescription("Precision-crafted formulas for strength, nerve support and metabolic balance.");               categoryRepository.save(supps);
        weight.setDescription("Botanical blends for metabolic balance and a lighter, more energised you.");                  categoryRepository.save(weight);
        skin.setDescription("Luxury botanical formulations that restore radiance and youthful vitality.");                   categoryRepository.save(skin);
        hair.setDescription("Advanced scalp and strand therapies bridging traditional wisdom with cellular science.");       categoryRepository.save(hair);

        // ── SUPPLEMENTS ─────────────────────────────────────────────────────────
        upsertProduct(
            new String[]{"Nerve Support"},
            "Nerve Support", supps, 2799.00,
            "Advanced nerve & energy formula with B-vitamins, antioxidants, and herbal support",
            "Nerve Support is an advanced nerve and energy formula crafted to support the nervous system, boost energy levels, and promote overall well-being. Packed with essential B-vitamins (B1, B2, B6, B12) for nerve health, calcium for bone and nerve integrity, R-alpha lipoic acid for antioxidant protection, and chromium for balanced blood sugar. Herbal components like peppermint leaf powder and kelp further enhance this holistic supplement.",
            "Vitamin B1 · Vitamin B2 · Vitamin B6 · Vitamin B12 · Calcium · R-Alpha Lipoic Acid · Chromium · Protease Enzyme · Lipase Enzyme · Peppermint Leaf Powder · Kelp Powder (Natural Iodine)",
            "• Promotes healthy nerve function and regeneration\n• Helps reduce numbness, tingling, and nerve discomfort\n• Supports mental clarity, positive mood, and emotional balance\n• Enhances energy production and combats fatigue\n• Strengthens bones and nervous system connectivity\n• Aids blood sugar balance and insulin sensitivity\n• Improves nutrient absorption for better overall health\n• Offers antioxidant protection to reduce oxidative stress",
            "Morning: Take one tablet after food. Night: Take one tablet after food. Use every day for best results, or as directed by a healthcare professional. Tip: Do not skip doses. Regular daily use is key to experiencing the full benefits.",
            "/assets/images/muscle strength .jpeg", true, false, false, false
        );

        upsertProduct(
            new String[]{"Muscle Strength", "Muscle Support"},
            "Muscle Support", supps, 3299.00,
            "Comprehensive muscle, bone & energy formula for strength and recovery",
            "Muscle & Bone Support Formula is a comprehensive blend of essential vitamins and minerals designed to support muscle function, energy production, bone strength, and overall wellness. With Magnesium, Calcium, Zinc, Iron, and fat-soluble vitamins like D3, K2, A, and E, this supplement targets both the structural and functional components of musculoskeletal health. Ideal for fitness enthusiasts, athletes, seniors, and anyone looking to stay strong, active, and energised.",
            "Magnesium · Calcium · Zinc · Iron · Vitamin D3 · Vitamin K2 · Vitamin A · Vitamin E",
            "• Helps prevent muscle cramps, spasms, and fatigue\n• Supports healthy muscle growth, repair, and recovery\n• Enhances bone density and strength for active lifestyles\n• Aids in oxygen delivery and sustained energy production\n• Protects against oxidative stress and inflammation\n• Contributes to immune system strength and metabolic function",
            "Morning: Take one tablet after food. Night: Take one tablet after food. Use every day for best results. Tip: Pair with regular physical activity and a balanced diet for optimal muscle and bone health.",
            "/assets/images/muscle strength .jpeg", false, false, true, false
        );

        upsertProduct(
            new String[]{"Joint Strength", "Joint Support"},
            "Joint Support", supps, 2499.00,
            "Science-backed joint formula for flexibility, comfort, and lasting mobility",
            "Advanced Joint Support Formula combines science-backed ingredients that promote joint comfort, flexibility, and long-term mobility. With powerful anti-inflammatory botanicals like Boswellia, Curcumin, Willow Bark, and Ginger Root, along with structural nutrients like Glucosamine, Chondroitin, and Hydrolysed Collagen Peptides, this formula supports the rebuilding and protection of cartilage and connective tissue.",
            "Vitamin C · Vitamin D3 · Vitamin K2 · Calcium Citrate · Glucosamine Sulfate · Chondroitin Sulfate · Hydrolysed Collagen Peptide · Sodium Hyaluronic Acid · Boswellia Serrata Extract · Curcumin Extract (Turmeric) · Willow Bark Extract · Ginger Root Extract · Olive Leaf Extract · Black Pepper Extract (Bioperine)",
            "• Helps reduce joint pain, stiffness, and inflammation\n• Supports healthy cartilage repair and joint cushioning\n• Improves mobility and flexibility for active daily living\n• Promotes strong bones and muscle coordination\n• Enhances nutrient absorption for maximum effectiveness\n• Offers natural, holistic support for joint longevity",
            "Morning: Take one tablet after food. Night: Take one tablet after food. Use every day for best results. Tip: Stay hydrated and maintain gentle movement for the best joint outcomes.",
            "/assets/images/joint strenth.jpeg", false, false, true, false
        );

        // ── WEIGHT LOSS ─────────────────────────────────────────────────────────
        upsertProduct(
            new String[]{"Fitblend", "FitBlend"},
            "FitBlend", weight, 1999.00,
            "Smart plant-based nutrition for weight loss, senior energy & fitness performance",
            "FitBlend is a smart, plant-based nutritional supplement crafted to meet the diverse needs of modern lifestyles. Whether you are on a weight loss journey, an older adult looking to boost daily energy, or an athlete needing clean fuel, FitBlend delivers balanced nourishment from natural sources. 100% Vegan · Dairy-Free · Gluten-Free · Soy-Free · No Artificial Additives.",
            "Vitamin B Complex · Rhodiola · Ashwagandha · Vitamin C · Zinc · Magnesium · Folate · Acerola Fruit · Bilberry Fruit · Dandelion Flower · Functional Mushrooms",
            "• Aids safe and sustained weight management without nutrient deficiency\n• Boosts daily energy levels, especially beneficial for older adults\n• Enhances endurance, muscle strength, and recovery for fitness and sports\n• Supports metabolic health, stabilising blood sugar and blood pressure naturally\n• Promotes digestive balance and improves gut health\n• Suitable for vegan, elderly, fitness-conscious, and diabetic-friendly diets",
            "For Weight Loss: Mix 2 tablespoons with 500ml of water and 1 tsp chia seeds. Consume twice daily — morning and night. Do not take solid food after consuming FitBlend. For Energy Support: Mix in 300ml of water, twice daily before food. Tip: Consistency is key. Use daily without skipping for best results.",
            null, false, false, false, true
        );

        // ── PAIN RELIEF ─────────────────────────────────────────────────────────
        upsertProduct(
            new String[]{"Sanjeevi Oil", "Vegam Sanjeevi Traditional Pain Relief Oil"},
            "Vegam Sanjeevi Traditional Pain Relief Oil", pain, 899.00,
            "100% Siddha-based herbal oil for joints, muscles, and deep tissue relief",
            "Vegam Sanjeevi Pain Relief Oil is a 100% Siddha-based herbal formulation designed to provide fast, natural relief from joint pain, muscle soreness, sprains, swelling, arthritis, and clot-related discomfort. Rooted in the ancient Siddha system of medicine, this oil blends powerful botanical ingredients known for their anti-inflammatory, pain-relieving, and circulation-enhancing properties. Safe for all age groups.",
            "Traditional Siddha Herbal Blend · Anti-inflammatory Botanicals · Circulation-Enhancing Herbs · 100% Chemical-Free Formula. Full ingredient list available on product packaging.",
            "• Provides natural and lasting pain relief without side effects\n• Relieves joint stiffness, inflammation, and swelling\n• Promotes mobility and comfort for active daily living\n• Speeds up healing of sprains and minor injuries\n• A reliable non-surgical option for managing chronic orthopaedic conditions\n• Improves blood circulation and supports healing of damaged tissues",
            "Apply a small amount of oil to the affected area. Massage gently in circular motions until absorbed. Use regularly as part of your daily self-care routine for optimal results. For external use only. Safe for all age groups. No rinsing required.",
            "/assets/images/sanjeevi pain realif .jpeg", true, false, false, false
        );

        upsertProduct(
            new String[]{"Unleash Gel", "Unleash Pain Relief Gel"},
            "Unleash Pain Relief Gel", pain, 1299.00,
            "Fast-acting natural gel for muscle and joint pain — athlete approved",
            "Unleash is a powerful, fast-acting topical gel designed to relieve joint pain, muscle soreness, sprains, and everyday aches. Infused with natural pain-relieving ingredients, it delivers deep, effective relief without relying on harsh chemicals. Endorsed by athletes and recommended by medical professionals, Unleash is your reliable partner in pain management.",
            "Herbal Anti-inflammatory Extracts · Natural Pain-Relieving Botanicals · Circulation-Boosting Compounds. Full ingredient list available on product packaging.",
            "• Provides fast, targeted relief from joint and muscle pain\n• Reduces inflammation and swelling effectively\n• Supports quick recovery from sprains and strains\n• Helps maintain mobility and comfort during physical activity\n• Offers a natural, chemical-free alternative to conventional painkillers\n• Boosts circulation, aiding the body's natural healing process",
            "Clean the affected area. Apply a small amount of gel. Massage in circular motions until fully absorbed. Wash hands after applying. Use up to 6 times daily. Do not apply on broken or irritated skin. Consult a doctor if pain persists. Storage: Keep in a cool, dry place. For external use only.",
            "/assets/images/panin realif gell.jpeg", false, true, false, false
        );

        upsertProduct(
            new String[]{"Hair Oil", "Vegam Revitalising Hair Oil"},
            "Vegam Revitalising Hair Oil", hair, 699.00,
            "Biotin, marine collagen & Ayurvedic herbs for stronger, fuller, healthier hair",
            "Vegam Revitalising Hair Oil is a holistic hair care solution inspired by the ancient wisdom of Siddha medicine and powered by modern botanical science. Expertly crafted with Biotin, Marine Collagen, and a potent blend of 9 herbal extracts, this nourishing oil supports stronger, thicker, and healthier hair — naturally. Free from harsh chemicals and synthetic fragrances.",
            "Biotin (Vitamin B7) · Marine Collagen · Rose Hip Fruit Extract · Green Tea Leaf Extract · Sesbania Leaf Extract · Aloe Vera Extract · Moringa Leaf Extract · Brahmi Flower Extract · Chamomile",
            "• Supports keratin production for stronger, thicker, and healthier hair\n• Reduces hair fall and supports natural regrowth\n• Fights oxidative stress — a key factor in premature hair loss\n• Soothes and hydrates the scalp for an ideal hair growth environment\n• Strengthens hair follicles and improves elasticity\n• Restores natural shine and volume with regular use",
            "Apply the oil twice a week. Massage into the scalp and along the hair length. Leave for at least 1 hour before washing with a mild shampoo. Avoid sun exposure while the oil is on your hair. Suitable for both men and women.",
            "/assets/images/hair oil .jpeg", true, false, false, false
        );

        upsertProduct(
            new String[]{"Neuro Flo", "NeuroFlô Hand & Foot Soak"},
            "NeuroFlô Hand & Foot Soak", pain, 1499.00,
            "Premium aromatic soak blend to relax tired muscles and refresh hands and feet",
            "NeuroFlô is a premium hand and foot soak formulation designed to deliver a soothing and restorative self-care experience. Infused with a carefully selected blend of essential oils, this concentrated foot therapy additive helps relax tired muscles, ease tension, and refresh overworked hands and feet. Its fast-acting aromatic formula promotes a calming sensation while supporting overall foot wellness.",
            "Mentha Piperita (Peppermint) Oil · Zingiber Officinale (Ginger Root) Oil · Eucalyptus Globulus Leaf Oil · Piper Nigrum (Black Pepper) Oil · Rosmarinus Officinalis (Rosemary) Leaf Oil · Syzygium Aromaticum (Clove) Bud Oil · Polysorbate 80 · Simmondsia Chinensis (Jojoba) Seed Oil · Caprylic/Capric Triglyceride",
            "• Helps relieve tired, stressed feet and hands\n• Provides a soothing and relaxing soak experience\n• Supports muscle relaxation and reduces discomfort\n• Refreshes and revitalises with natural essential oils\n• Easy-to-use concentrated formula for quick results",
            "Add 5 drops to 3–5 litres of warm water. Soak feet or hands for 15–20 minutes. Use once daily or as needed. Tip: For a deeper relaxing experience, add to warm (not hot) water and use in the evening before bed.",
            "/assets/images/pain relif.jpeg", false, false, false, true
        );

        // ── SKIN CARE ────────────────────────────────────────────────────────────
        upsertProduct(
            new String[]{"Face Wash (SKIN CARE NO:1)", "SKNCARE No. 1 Powder Cleanse"},
            "SKNCARE No. 1 Powder Cleanse", skin, 399.00,
            "Gentle powder-to-foam facial cleanser for clear, balanced, oil-free skin",
            "SKNCARE No. 1 Powder Cleanse is a gentle powder-to-foam facial cleanser designed for daily use. It effectively removes excess oil, unclogs pores, and helps maintain clear-looking skin without disturbing the skin's natural balance. Suitable for all skin types, especially oily and acne-prone skin.",
            "Oryza Sativa (Rice) Flour · Zea Mays (Corn) Starch · Kaolin · Sodium Cocoyl Isethionate · Sodium Lauroyl Sarcosinate · Oryza Sativa (Rice Bran) Powder · Avena Sativa (Oat) Kernel Flour · Lac Powder · Rosa Damascena Flower Powder · Niacinamide · Aloe Barbadensis Leaf Juice Extract · Glycyrrhiza Glabra (Licorice) Root Extract · Zinc Oxide · Silica · Magnesium Stearate · Allantoin",
            "• Gently cleanses without stripping natural moisture\n• Helps remove excess oil and impurities\n• Supports clearer, balanced-looking skin\n• Ideal for oily and acne-prone skin types\n• Soft powder transforms into a mild foaming cleanser\n• Free from soap, sulphates, parabens, and added colour",
            "Dispense a small amount of powder into wet palms. Add water and lather gently. Massage onto damp face. Rinse thoroughly. Use once or twice daily. Suitable for all skin types.",
            "/assets/images/skin care 1 .jpeg", true, false, false, false
        );

        // Preserve other skin care products
        upsertProduct(new String[]{"Serum (SKIN CARE NO:2)"}, "Serum (SKIN CARE NO:2)", skin, 799.00, "Intensive radiance boosting treatment", "A concentrated serum that delivers intensive radiance-boosting actives deep into the skin. Formulated with botanical brighteners and hydrating agents for a luminous, even complexion.", "Botanical Brightening Complex · Hyaluronic Acid · Vitamin C · Niacinamide · Plant Stem Cell Extracts", "• Visibly brightens and evens skin tone\n• Delivers deep hydration\n• Reduces appearance of dark spots\n• Supports collagen production", "Apply 2-3 drops to clean skin morning and night. Gently press into skin. Follow with moisturiser. For best results use consistently.", null, false, true, false, false);
        upsertProduct(new String[]{"Moisturizor (SKIN CARE NO:3)"}, "Moisturizor (SKIN CARE NO:3)", skin, 649.00, "Deep hydration and barrier protection", "A rich, deeply nourishing moisturiser that replenishes the skin's natural moisture barrier. Infused with botanical lipids and humectants for sustained hydration throughout the day.", "Shea Butter · Botanical Lipid Complex · Hyaluronic Acid · Ceramides · Aloe Vera · Vitamin E", "• Provides deep, long-lasting hydration\n• Restores and strengthens the skin barrier\n• Soothes and calms sensitive skin\n• Non-greasy, fast-absorbing formula", "Apply to clean skin morning and evening. Massage gently until fully absorbed.", null, false, false, false, false);
        upsertProduct(new String[]{"Sunscreen (SKIN CARE NO:4)"}, "Sunscreen (SKIN CARE NO:4)", skin, 549.00, "SPF 50 Mineral-based daily shield", "A broad-spectrum SPF 50 mineral sunscreen that provides superior protection against UVA and UVB rays. Lightweight, non-comedogenic, and suitable for all skin types including sensitive skin.", "Zinc Oxide · Titanium Dioxide · Aloe Vera · Vitamin E · Botanical Soothing Extracts", "• Provides broad-spectrum SPF 50 UV protection\n• Non-comedogenic, won't clog pores\n• Lightweight formula suitable for daily use\n• Soothes and hydrates while protecting", "Apply generously to face and exposed skin 15 minutes before sun exposure. Reapply every 2 hours or after swimming/sweating.", null, false, false, false, true);

        // ── HAIR CARE ────────────────────────────────────────────────────────────
        upsertProduct(
            new String[]{"Cream (HAIR CARE NO:2)", "HRCARE No. 1 Hair Oil Cream"},
            "HRCARE No. 1 Hair Oil Cream", hair, 599.00,
            "Rich overnight hair treatment for deep scalp nourishment and stronger roots",
            "HRCARE No. 1 Hair Oil Cream is a rich overnight treatment designed to deeply nourish the scalp and roots. Formulated with a blend of botanical oils, fatty acids, and conditioning agents, it helps support stronger, healthier-looking hair with regular use. A modern alternative to traditional hair oil delivering deep hydration.",
            "Aqua · Cocos Nucifera (Coconut) Oil · Cucurbita Pepo (Pumpkin) Seed Oil · Simmondsia Chinensis (Jojoba) Seed Oil · Paraffinum Liquidum · Glyceryl Stearate & Cetearyl Alcohol · Stearic Acid · Cetyl Alcohol · Glycerin · BTMS-50 · Rosmarinus Officinalis (Rosemary) Oil · Tocopheryl Acetate (Vitamin E) · Preservative · Citric Acid",
            "• Deeply nourishes scalp and hair roots\n• Helps improve hair strength and overall texture\n• Supports healthier, smoother, and more manageable hair\n• Provides intense overnight hydration\n• Lightweight cream format for easy application\n• A modern, non-greasy alternative to traditional hair oils",
            "Apply a small amount directly to the scalp. Massage gently into the roots using fingertips. Leave overnight or for at least 30 minutes before washing. Use 2–3 times per week for best results. Tip: Apply before bed and wash off in the morning for deep conditioning.",
            "/assets/images/haircre 2 .jpeg", false, false, false, false
        );

        upsertProduct(
            new String[]{"Shampoo (HAIR CARE NO:1)", "HRCARE No. 2 Shampoo"},
            "HRCARE No. 2 Shampoo", hair, 499.00,
            "Gentle hydrating shampoo enriched with proteins and botanicals for healthy hair",
            "HRCARE No. 2 Shampoo is a gentle, hydrating cleanser designed to cleanse without stripping the hair's natural moisture. Enriched with panthenol, hydrolysed keratin, oat protein, and botanical extracts, it helps improve softness, manageability, and overall hair health. Ideal for dry, damaged, and chemically treated hair.",
            "Aqua · Lavandula Angustifolia (Lavender) Flower Water · Glycerin · Cyamopsis Tetragonoloba (Guar) Gum · Polyquaternium-7 · Allantoin · Sodium Lauroyl Sarcosinate · Decyl Glucoside · Cocamidopropyl Betaine · Coco Glucoside · PEG-120 Methyl Glucose Dioleate · Panthenol · Aloe Barbadensis Leaf Extract · Hydrolysed Oat Protein · Hydrolysed Keratin · Calendula Officinalis Flower Extract · Tocopheryl Acetate · Citrus Bergamia (Bergamot) Peel Oil · Citric Acid",
            "• Gently cleanses without drying out hair\n• Helps restore moisture and hydration\n• Improves softness and manageability\n• Supports repair of dry and damaged hair\n• Suitable for chemically treated hair\n• Enriched with proteins and botanical extracts",
            "Apply to wet hair and scalp. Gently massage to create a rich lather. Rinse thoroughly. Repeat if required. Use regularly for best results. Tip: Ideal for dry, damaged, and chemically treated hair. Follow with a conditioner for extra softness.",
            null, false, false, false, false
        );

        // Preserve remaining hair care products
        upsertProduct(new String[]{"Serum (HAIR CARE NO:3)"}, "Serum (HAIR CARE NO:3)", hair, 699.00, "Gloss boosting and damage repair serum", "A lightweight hair serum that seals the cuticle, eliminates frizz, and delivers an intense glossy finish. Infused with silk proteins and botanical extracts for exceptional shine and repair.", "Silk Amino Acids · Argan Oil · Vitamin E · Botanical Keratin Complex · UV Filters", "• Seals split ends and reduces frizz\n• Adds intense gloss and shine\n• Protects against heat and UV damage\n• Lightweight formula suitable for all hair types", "Apply a few drops to damp or dry hair, focusing on mid-lengths to ends. Style as usual.", null, false, true, false, false);
        upsertProduct(new String[]{"Conditioner (HAIR CARE NO:4)"}, "Conditioner (HAIR CARE NO:4)", hair, 449.00, "Hydrating botanical conditioner", "A luxuriously creamy conditioner that instantly detangles, softens, and hydrates every strand. Rich in botanical butters and amino acids for long-lasting smoothness.", "Shea Butter · Coconut Oil · Panthenol · Hydrolysed Wheat Protein · Aloe Vera · Lavender Oil", "• Provides intense moisture and detangling\n• Improves softness and reduces breakage\n• Seals the hair cuticle for enhanced shine\n• Suitable for all hair types", "Apply to freshly washed hair after shampooing. Leave for 2-3 minutes then rinse thoroughly.", null, false, false, false, false);
        upsertProduct(new String[]{"Hair Mask (HAIR CARE NO:5)"}, "Hair Mask (HAIR CARE NO:5)", hair, 899.00, "Intensive weekly repair ritual", "An intensive weekly hair mask that provides a deep repair treatment for severely damaged, over-processed, or colour-treated hair. Infused with potent botanical actives to restore strength and vitality.", "Hydrolysed Keratin · Argan Oil · Macadamia Oil · Shea Butter · Biotin · Vitamin B5 · Bamboo Extract", "• Deeply repairs and restores severely damaged hair\n• Rebuilds hair structure from within\n• Dramatically improves strength and elasticity\n• Restores shine and manageability to colour-treated hair", "Apply generously to clean, damp hair from roots to tips. Leave for 10-20 minutes. Rinse thoroughly. Use once a week.", null, false, false, true, false);

        // ── Hidden store review product ───────────────────────────────────────────
        Product storeReviewProduct = productRepository.findByName("Signature Store Experience").orElse(new Product());
        if (storeReviewProduct.getProductId() == null) {
            storeReviewProduct.setName("Signature Store Experience");
            storeReviewProduct.setSubtitle("Share your in-store experience with the Truly Bare community.");
            storeReviewProduct.setDescription("A virtual listing to capture customer feedback on the Truly Bare store experience. Not for sale.");
            storeReviewProduct.setPrice(java.math.BigDecimal.ZERO);
            storeReviewProduct.setStockQuantity(0);
            storeReviewProduct.setIsActive(false);
            productRepository.save(storeReviewProduct);
            System.out.println("✅ Signature Store Experience (virtual review product) seeded.");
        }

        System.out.println("✅ Vegam Product Guide 2025 — Full catalog seeded!");
    }

    /** Find product by any of the oldNames (handles renames), then save with new name + full details. */
    private void upsertProduct(String[] searchNames, String newName, Category category, double price,
                               String subtitle, String description, String ingredients, String benefits,
                               String usage, String imageUrl,
                               boolean bestSeller, boolean isNew, boolean isClassic, boolean featured) {
        if (category == null) return;
        Product p = null;
        for (String sn : searchNames) {
            p = productRepository.findByName(sn).orElse(null);
            if (p != null) break;
        }
        if (p == null) p = new Product();

        p.setName(newName);
        p.setCategory(category);
        p.setSubtitle(subtitle);
        p.setDescription(description);
        p.setIngredients(ingredients);
        p.setBenefits(benefits);
        p.setUsageInstructions(usage);
        p.setWarnings("Store in a cool, dry place away from direct sunlight. Keep out of reach of children. Discontinue use if irritation occurs and consult a healthcare professional.");
        p.setPrice(BigDecimal.valueOf(price));
        if (p.getProductId() == null) p.setStockQuantity(100);
        p.setIsActive(true);
        if (imageUrl != null && (p.getImageUrl() == null || p.getImageUrl().isBlank())) p.setImageUrl(imageUrl);
        p.setIsBestSeller(bestSeller);
        p.setIsNew(isNew);
        p.setIsClassic(isClassic);
        p.setIsFeatured(featured);
        productRepository.save(p);
    }

    private Category save(Category c) { return c; }

    private Category createCategoryIfMissing(String name, Category parent) {
        Category c = categoryRepository.findByName(name).orElse(new Category());
        if (c.getCategoryId() == null) { c.setName(name); c.setIsActive(true); }
        c.setParentCategory(parent);
        return categoryRepository.save(c);
    }
}

