-- ============================================================
-- TRULY BARE - Product Import Script (VEGAM 2025)
-- ============================================================

USE truly_bare;

-- Disable foreign key checks for clean insertion
SET FOREIGN_KEY_CHECKS = 0;

-- Delete any existing products with these names to avoid duplicates if run multiple times
DELETE FROM products WHERE name IN (
    'Nerve Support', 'Muscle Support', 'Joint Support', 'FitBlend', 
    'Vegam Sanjeevi Pain Relief Oil', 'Unleash Pain Relief Gel', 'Vegam Revitalising Hair Oil'
);

-- Insert Products
INSERT INTO products (
    name, 
    subtitle, 
    description, 
    ingredients, 
    benefits, 
    usage_instructions, 
    price, 
    stock_quantity, 
    category_id, 
    is_active, 
    is_featured,
    created_at,
    updated_at
) VALUES 
-- 1. Nerve Support
(
    'Nerve Support', 
    'Advanced nerve & energy formula with B-vitamins, antioxidants, and herbal support', 
    'Nerve Support is an advanced nerve and energy formula thoughtfully crafted to support the nervous system, boost energy levels, and promote overall well-being. Packed with essential B-vitamins (B1, B2, B6, B12) for nerve health and neurotransmitter function, the formula also includes calcium for bone and nerve integrity, R-alpha lipoic acid for antioxidant protection, and chromium for balanced blood sugar levels.', 
    'Vitamin B1, Vitamin B2, Vitamin B6, Vitamin B12, Calcium, R-Alpha Lipoic Acid, Chromium, Protease Enzyme, Lipase Enzyme, Peppermint Leaf Powder, Kelp Powder (Natural Iodine)', 
    '• Promotes healthy nerve function and regeneration\n• Helps reduce numbness, tingling, and nerve discomfort\n• Supports mental clarity, positive mood, and emotional balance\n• Enhances energy production and combats fatigue\n• Strengthens bones and nervous system connectivity\n• Aids in blood sugar balance and insulin sensitivity\n• Improves nutrient absorption for better overall health\n• Offers antioxidant protection to reduce oxidative stress', 
    '1. Morning: Take one tablet after food.\n2. Night: Take one tablet after food.\n3. Consistency: Use every day for best results.\nTip: Do not skip doses.', 
    1250.00, 100, 1, 1, 1, NOW(), NOW()
),
-- 2. Muscle Support
(
    'Muscle Support', 
    'Comprehensive muscle, bone & energy formula for strength and recovery', 
    'Muscle & Bone Support Formula is a comprehensive blend of essential vitamins and minerals designed to support muscle function, energy production, bone strength, and overall wellness. Ideal for fitness enthusiasts, athletes, seniors, and anyone looking to stay strong, active, and energised throughout the day.', 
    'Magnesium, Calcium, Zinc, Iron, Vitamin D3, Vitamin K2, Vitamin A, Vitamin E', 
    '• Helps prevent muscle cramps, spasms, and fatigue\n• Supports healthy muscle growth, repair, and recovery\n• Enhances bone density and strength for active lifestyles\n• Aids in oxygen delivery and sustained energy production\n• Protects against oxidative stress and inflammation\n• Contributes to immune system strength and metabolic function', 
    '1. Morning: Take one tablet after food.\n2. Night: Take one tablet after food.\n3. Consistency: Use every day for best results.\nTip: Pair with regular physical activity.', 
    1150.00, 100, 1, 1, 0, NOW(), NOW()
),
-- 3. Joint Support
(
    'Joint Support', 
    'Science-backed joint formula for flexibility, comfort, and lasting mobility', 
    'Advanced Joint Support Formula combines science-backed ingredients that promote joint comfort, flexibility, and long-term mobility. With powerful anti-inflammatory botanicals like Boswellia, Curcumin, Willow Bark, and Ginger Root, along with structural nutrients like Glucosamine, Chondroitin, and Hydrolysed Collagen Peptides.', 
    'Vitamin C, Vitamin D3, Vitamin K2, Calcium Citrate, Glucosamine Sulfate, Chondroitin Sulfate, Hydrolysed Collagen Peptide, Sodium Hyaluronic Acid, Boswellia Serrata Extract, Curcumin Extract (Turmeric), Willow Bark Extract, Ginger Root Extract, Olive Leaf Extract, Black Pepper Extract (Bioperine)', 
    '• Helps reduce joint pain, stiffness, and inflammation\n• Supports healthy cartilage repair and joint cushioning\n• Improves mobility and flexibility for active daily living\n• Promotes strong bones and muscle coordination\n• Enhances nutrient absorption for maximum effectiveness', 
    '1. Morning: Take one tablet after food.\n2. Night: Take one tablet after food.\n3. Consistency: Use every day for best results.\nTip: Stay hydrated.', 
    1450.00, 100, 1, 1, 1, NOW(), NOW()
),
-- 4. FitBlend
(
    'FitBlend', 
    'Smart plant-based nutrition for weight loss, senior energy & fitness performance', 
    'FitBlend is a smart, plant-based nutritional supplement crafted to meet the diverse needs of modern lifestyles. Whether you are on a weight loss journey, an older adult looking to boost daily energy, or an athlete needing clean fuel, FitBlend delivers balanced nourishment from natural sources.', 
    'Vitamin B Complex, Rhodiola, Ashwagandha, Vitamin C, Zinc, Magnesium, Folate, Acerola Fruit, Bilberry Fruit, Dandelion Flower, Functional Mushrooms', 
    '• Aids safe and sustained weight management\n• Boosts daily energy levels\n• Enhances endurance, muscle strength, and recovery\n• Supports metabolic health\n• Promotes digestive balance\n• 100% Vegan, Dairy-Free, Gluten-Free', 
    'For Weight Loss: Mix 2 tbsp with 500ml water and 1 tsp chia seeds. Consume twice daily. No solid food after.\nFor Energy: Mix in 300ml water. Consume twice daily before food.', 
    1850.00, 100, 2, 1, 1, NOW(), NOW()
),
-- 5. Vegam Sanjeevi Pain Relief Oil
(
    'Vegam Sanjeevi Pain Relief Oil', 
    '100% Siddha-based herbal oil for joints, muscles, and deep tissue relief', 
    'Vegam Sanjeevi Pain Relief Oil is a 100% Siddha-based herbal formulation designed to provide fast, natural relief from joint pain, muscle soreness, sprains, swelling, arthritis, and clot-related discomfort. Safe for all age groups.', 
    'Traditional Siddha Herbal Blend, Anti-inflammatory Botanicals, Circulation-Enhancing Herbs', 
    '• Provides natural and lasting pain relief\n• Relieves joint stiffness, inflammation, and swelling\n• Promotes mobility and comfort\n• Speeds up healing of sprains\n• Non-surgical option for chronic conditions', 
    '1. Apply: A small amount to affected area.\n2. Massage: Gently in circular motions until absorbed.\n3. Routine: Use regularly for optimal results.', 
    450.00, 100, 5, 1, 1, NOW(), NOW()
),
-- 6. Unleash Pain Relief Gel
(
    'Unleash Pain Relief Gel', 
    'Fast-acting natural gel for muscle and joint pain — athlete approved', 
    'Unleash is a powerful, fast-acting topical gel designed to relieve joint pain, muscle soreness, sprains, and everyday aches. Infused with natural pain-relieving ingredients, it delivers deep, effective relief without harsh chemicals.', 
    'Herbal Anti-inflammatory Extracts, Natural Pain-Relieving Botanicals, Circulation-Boosting Compounds', 
    '• Provides fast, targeted relief\n• Reduces inflammation and swelling\n• Supports quick recovery from sprains\n• Athlete approved and chemical-free', 
    '1. Clean: Wash and dry area.\n2. Apply: Squeeze small amount.\n3. Massage: Rub until absorbed.\n4. Repeat: Use up to 6 times daily.', 
    350.00, 100, 5, 1, 0, NOW(), NOW()
),
-- 7. Vegam Revitalising Hair Oil
(
    'Vegam Revitalising Hair Oil', 
    'Biotin, marine collagen & Ayurvedic herbs for stronger, fuller, healthier hair', 
    'Vegam Revitalising Hair Oil is a holistic hair care solution inspired by the ancient wisdom of Siddha medicine and powered by modern botanical science. Expertly crafted with Biotin, Marine Collagen, and a potent blend of 9 herbal extracts.', 
    'Biotin (Vitamin B7), Marine Collagen, Rose Hip Fruit Extract, Green Tea Leaf Extract, Sesbania Leaf Extract, Aloe Vera Extract, Moringa Leaf Extract, Brahmi Flower Extract, Chamomile', 
    '• Supports keratin production for stronger hair\n• Reduces hair fall and supports regrowth\n• Fights oxidative stress\n• Soothes and hydrates the scalp\n• Improves elasticity and volume', 
    '1. Frequency: Apply twice a week.\n2. Apply: Massage into scalp and hair length.\n3. Leave On: Keep for at least 1 hour.\n4. Caution: Avoid sun exposure while oil is on.', 
    650.00, 100, 4, 1, 1, NOW(), NOW()
);

SET FOREIGN_KEY_CHECKS = 1;

-- Final check
SELECT product_id, name, category_id FROM products;
