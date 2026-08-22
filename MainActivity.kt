package com.bembatranslate.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

private data class Entry(val english: String, val bemba: String, val category: String, val note: String = "")

private data class Conversation(val triggers: List<String>, val bemba: String, val englishReply: String = "", val suggestion: String = "")


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var content: LinearLayout
    private lateinit var search: EditText
    private lateinit var tts: TextToSpeech
    private val favorites = mutableSetOf<String>()
    private var selectedCategory = "All"
    private var showingDictionary = false
    private var englishToBemba = true
    private val history = mutableListOf<String>()

    private val entries = listOf(
        Entry("How are you?", "Uli shani?", "Greetings", "Formal/plural: Muli shani?"),
        Entry("Good morning", "Mwashibukeni!", "Greetings"),
        Entry("Hello / Hi", "Mwapola / Mwapoleni", "Greetings", "Use the form appropriate to the person and context."),
        Entry("I am fine", "Ndifye bwino", "Greetings"),
        Entry("Fine, and you?", "Bwino, ngaiwe?", "Greetings"),
        Entry("Thank you", "Natotela", "Greetings"),
        Entry("Thanks a lot", "Natotela saana", "Greetings"),
        Entry("Yes", "Ee / Eya", "Basic"), Entry("No", "Awe", "Basic"),
        Entry("Welcome", "Mwaiseni", "Greetings"), Entry("Good afternoon", "Kasuba mukwai", "Greetings"),
        Entry("Good evening", "Chungulo mukwai", "Greetings"), Entry("Good night", "Sendameenipo", "Greetings"),
        Entry("Sleep well", "Ulale umutende / Ulale bwino", "Greetings", "Formal: Mulale umutende / Mulale bwino."),
        Entry("What is the news?", "Kuli ci?", "Greetings", "Response: Kwatalala."),
        Entry("Goodbye", "Kafikenipo / Shaleenipo", "Greetings", "Fare well / stay well."),
        Entry("Fare well", "Wende umutende / Mwende umutende", "Greetings", "Informal / formal."),
        Entry("My name is...", "Ishina lyandi ni...", "Basic"),
        Entry("I don't know", "Nshishibe", "Basic"), Entry("Sorry / Forgive me", "Njeleleneko", "Basic"),
        Entry("Pardon me / Forgive me", "Munjeleleko", "Basic"), Entry("I am sorry / Show me mercy", "Mbelelako uluse", "Basic"),
        Entry("I am asking for forgiveness", "Ndelomba ubwelelo", "Basic"),
        Entry("Help", "Ubwafwilisho", "Basic"),
        Entry("Help me", "Ngafwa / Ngafwako / Ngafweni / Ngafweniko", "Basic"),
        Entry("I love you", "Nalikutemwa", "Phrases", "Very much: Nalikutemwa sana."),
        Entry("I miss you", "Nakufuluka / Nalefuluka", "Phrases", "Very much: Nakufuluka sana."),
        Entry("I want money", "Ndefwaya indalama", "Phrases"), Entry("Where are you?", "Ulikwisa", "Phrases"),
        Entry("Where are they?", "Balikwisa", "Phrases"), Entry("I am angry", "Nimfulwa", "Phrases"),
        Entry("Family", "Lupwa", "Family"), Entry("Loved one", "Mutemwikwa", "People"), Entry("Student", "Musambi", "People"),
        Entry("Person", "Umuntu", "People"), Entry("Friend", "Chibusa", "People", "Also Cibusa / Icibusa."),
        Entry("Friends", "Ifibusa", "People"), Entry("Child", "Umwana", "Family"), Entry("Children", "Abaana", "Family"),
        Entry("Father", "Tata", "Family"), Entry("My father", "Ba Tata", "Family"), Entry("Our father", "Shifwe", "Family"),
        Entry("Your father", "Wiso", "Family", "Formal: Ba Wiso; plural/formal: Shinwe."), Entry("His / her father", "Wishi", "Family", "Formal: Ba wishi."),
        Entry("Their father", "Shibo", "Family"), Entry("Mother", "Mayo", "Family"), Entry("Our mother", "Nyinefwe", "Family"),
        Entry("Your mother", "Noko", "Family", "Formal: Ba Noko."), Entry("His / her mother", "Nyina", "Family", "Formal: Ba Nyina."),
        Entry("Their mother", "Nyinabo", "Family"), Entry("Brother", "Ndume nandi", "Family"), Entry("Sister", "Nkashi", "Family"),
        Entry("Husband", "Mûlume", "Family"), Entry("Wife", "Mûkashi", "Family"), Entry("Grandfather", "Shikulu", "Family"),
        Entry("Uncle", "Tata mwaice", "Family", "Paternal uncle; formal: Ba Tata mwaice."), Entry("Aunt", "Mayosenge", "Family", "Paternal aunt; formal: Ba Mayo senge."),

        Entry("Lion", "Inkalamo", "Animals"), Entry("Elephant", "Insofu", "Animals"), Entry("Leopard", "Imbwili", "Animals"),
        Entry("Monkey", "Kolwe", "Animals"), Entry("Rabbit / Hare", "Kalulu", "Animals"), Entry("Hyena", "Cimbwi", "Animals"),
        Entry("Buffalo", "Imboo", "Animals"), Entry("Hippopotamus", "Mfubu", "Animals"), Entry("Crocodile", "Ing'wena", "Animals"),
        Entry("Snake", "Insoka", "Animals"), Entry("Dog", "Imbwa", "Animals"), Entry("Cat", "Puushi", "Animals", "Côna is also used."),
        Entry("Cow / Cattle", "Ing'ombe", "Animals"), Entry("Goat", "Imbushi", "Animals"), Entry("Pig", "Inkumba", "Animals"),
        Entry("Chicken", "Inkoko", "Animals"), Entry("Horse", "Kabalwe", "Animals"), Entry("Donkey", "Punda", "Animals"),
        Entry("Bee", "Ulushimu", "Animals"), Entry("Cheetah", "Cinseketa", "Animals"), Entry("Ant", "Nyelele", "Animals"),
        Entry("Butterfly", "Icipelebesha", "Animals"), Entry("Bird", "Icuni", "Animals"), Entry("Camel", "Ingamiya", "Animals"),

        Entry("Head", "Umutwe", "Body", "Plural: Imitwe."), Entry("Eye", "Ilinso", "Body", "Plural: Amenso."), Entry("Nose", "Umoona", "Body", "Plural: Imyoona."),
        Entry("Nostril", "Umoona", "Body", "Plural: Imyoona."), Entry("Mouth", "Akanwa", "Body", "Plural: Utunwa."), Entry("Lip", "Umulomo", "Body", "Plural: Imilomo."),
        Entry("Eyebrow", "Inkopyo", "Body"), Entry("Chin", "Icilefulefu", "Body", "Plural: Ifilefulefu."), Entry("Cheek", "Itobo", "Body", "Plural: Amatobo."),
        Entry("Beard", "Umwefu", "Body", "Plural: Imyefu."), Entry("Hair", "Umushishi", "Body"), Entry("Tooth", "Ilino", "Body", "Plural: Ameno."),
        Entry("Tongue", "Ululimi", "Body", "Plural: Indimi."), Entry("Saliva", "Amate", "Body"), Entry("Breast", "Ibeele", "Body", "Plural: Amabeele."),
        Entry("Chest", "Ichifuba", "Body", "Plural: Ififuba."), Entry("Shoulder", "Ichipeeya", "Body", "Plural: Ifipeeya; also Ukubeya / Amabeya."),
        Entry("Arm / Hand", "Ukuboko", "Body", "Plural: Amaboko."), Entry("Finger", "Umunwe", "Body", "Plural: Iminwe."), Entry("Thumb", "Ichikumo", "Body", "Plural: Ifikumo."),
        Entry("Palm", "Ichisansa", "Body", "Plural: Ifisansa."), Entry("Skin", "Inkanda", "Body"), Entry("Belly", "Ifumo", "Body", "Plural: Amafumo."),
        Entry("Navel", "Umutoto", "Body", "Plural: Imitoto."), Entry("Neck", "Umukoshi", "Body", "Plural: Imikoshi."), Entry("Stomach", "Ichifu", "Body", "Plural: Ififu."),
        Entry("Intestine / Bowel", "Ubula", "Body", "Plural: Amala."), Entry("Blood", "Umulopa", "Body"), Entry("Heart", "Umutima", "Body", "Plural: Imitima."),
        Entry("Bladder", "Ichisu", "Body", "Plural: Ifisu."), Entry("Urine", "Imisu", "Body"), Entry("Rib", "Ulubafu", "Body", "Plural: Imbafu."),
        Entry("Spine", "Umungoloolo", "Body", "Plural: Imingoloolo."), Entry("Hip", "Intungu", "Body"), Entry("Gum", "Ichiponshi", "Body", "Plural: Ifiponshi."),
        Entry("Heel", "Ichitende", "Body", "Plural: Ifitende."), Entry("Bone", "Ifupa", "Body", "Plural: Amafupa."), Entry("Leg", "Ukuulu", "Body", "Plural: Amaoolu."),
        Entry("Toe", "Ichikondo", "Body", "Plural: Ifikondo."), Entry("Nail", "Ulwaala", "Body", "Plural: Amaala."), Entry("Buttocks", "Itako", "Body", "Plural: Amatako."),
        Entry("Anus", "Imputi / Umusula", "Body", "Plural: Imisula."), Entry("Pubic hair", "Amaso", "Body"), Entry("Vagina", "Ubwanakashi", "Body", "Formal; Ichinyo is informal."),
        Entry("Penis", "Ubwaume", "Body", "Formal; informal forms exist."), Entry("Clitoris", "Nini", "Body"), Entry("Waist", "Umusana", "Body", "Plural: Imisana."),
        Entry("Wrist", "Inkolokoso", "Body"), Entry("Forehead", "Impumi", "Body"),

        Entry("Black", "-fiita", "Adjectives"), Entry("Brown", "-kashikila", "Adjectives"), Entry("Red", "-kashika", "Adjectives"), Entry("White", "-buuta", "Adjectives"),
        Entry("Blue", "blue", "Adjectives"), Entry("Grey", "grey", "Adjectives"), Entry("Purple", "pepo", "Adjectives"), Entry("Orange", "olenji", "Adjectives"),
        Entry("Yellow", "yelo", "Adjectives"), Entry("Big", "-kulu", "Adjectives"), Entry("Small", "-nono / -cepa", "Adjectives"), Entry("Long / Tall", "-leepa", "Adjectives"),
        Entry("Short / Narrow", "-ipipa", "Adjectives"), Entry("Deep", "-shika", "Adjectives"), Entry("Sweet", "-lowa", "Adjectives"), Entry("Sour", "-sasamina", "Adjectives"), Entry("Bitter", "-lula", "Adjectives"),
        Entry("Good", "suma", "Adjectives"), Entry("All", "onse", "Adjectives"),

        Entry("One", "-mo", "Numbers"), Entry("Two", "-bili", "Numbers"), Entry("Three", "-tatu", "Numbers"), Entry("Four", "-ne", "Numbers"), Entry("Five", "sano", "Numbers"),
        Entry("Six", "mutanda", "Numbers"), Entry("Seven", "cine lubali", "Numbers"), Entry("Eight", "cine konse konse", "Numbers"), Entry("Nine", "paabula", "Numbers"), Entry("Ten", "ikumi", "Numbers"),
        Entry("Eleven", "ikumi na -mo", "Numbers"), Entry("Twelve", "ikumi na -bili", "Numbers"), Entry("Twenty", "ama kumi yabili", "Numbers"), Entry("Thirty", "ama kumi yatatu", "Numbers"),
        Entry("One hundred", "mwanda", "Numbers"), Entry("Five hundred", "imyaanda îsaano", "Numbers"), Entry("One thousand", "kana / ikana", "Numbers"), Entry("Twelve thousand", "ama kana ikumi na yabili", "Numbers"),

        Entry("Monday", "Pali chimo", "Days"), Entry("Tuesday", "Pali chibili", "Days"), Entry("Wednesday", "Pali chitatu", "Days"), Entry("Thursday", "Pali chine", "Days"),
        Entry("Friday", "Pali chisano", "Days"), Entry("Saturday", "Pa chibelushi", "Days"), Entry("Sunday", "Pa mulungu", "Days"),
        Entry("January", "Akabengele kanono", "Months"), Entry("February", "Akabengele kakalamba", "Months"), Entry("March", "Kutumpu", "Months"), Entry("April", "Shinde", "Months"),
        Entry("May", "Akapepo kanono", "Months"), Entry("June", "Akapepo kakalamba", "Months"), Entry("July", "Chikungu lupepo", "Months"), Entry("August", "Akasaka ntobo", "Months"),
        Entry("September", "Lusuba lunono", "Months"), Entry("October", "Lusuba lukalamba", "Months"), Entry("November", "Chinshikubili", "Months"), Entry("December", "Mupundu milimo", "Months"),

        // Additional corrected vocabulary supplied for the dictionary
        Entry("Night-adder", "Icilambanshila", "Snakes"), Entry("Spitting cobra", "Kafi", "Snakes"),
        Entry("Kanshimonamitenge", "Kanshimonamitenge", "Snakes"), Entry("Boomslang", "Ibalabala", "Snakes"),
        Entry("Puff-adder", "Ifwafwa", "Snakes"), Entry("Twig-snake", "Nalukunilumo", "Snakes"),
        Entry("Blind snake", "Luminuminu", "Snakes"), Entry("Python", "Lusato", "Snakes"),
        Entry("Forest cobra", "Maamba", "Snakes"), Entry("Water cobra", "Maambalushi", "Snakes"),
        Entry("Gaboon viper", "Mbooma", "Snakes"), Entry("Blind snake / Two-headed snake", "Mbulushi", "Snakes"),
        Entry("Hissing sand snake", "Mulalu", "Snakes"), Entry("File-snake", "Mwendalwali", "Snakes"),
        Entry("Egg-eating snake", "Namutukuta", "Snakes"), Entry("House snake", "Indele", "Snakes"),
        Entry("White-lipped snake", "Indele", "Snakes"), Entry("Common cobra", "Ngoshe", "Snakes"),
        Entry("Grey-beaked snake", "Ntunkamatumba", "Snakes"), Entry("Unknown snake", "Impini", "Snakes"),
        Entry("Unknown snake", "Iyongolo", "Snakes"), Entry("Unknown snake", "Itiya", "Snakes"),

        Entry("Fish eagle", "Cembe", "Birds"), Entry("Coracias", "Cikwekwe", "Birds"), Entry("Marabou stork", "Cipampa", "Birds"),
        Entry("Turtle dove", "Cipeele", "Birds"), Entry("Quail", "Cipingila", "Birds"), Entry("Owl", "Cipululu", "Birds"),
        Entry("Bateleur eagle", "Cipungu", "Birds"), Entry("Weaver", "Cisokopela", "Birds"), Entry("Wild duck", "Coso", "Birds"),
        Entry("Guinea fowl", "Ikanga", "Birds"), Entry("Vulture", "Ikubi", "Birds"), Entry("Snipe", "Kakandamatipa", "Birds"),
        Entry("Crowned plover", "Kakolenkole", "Birds"), Entry("Chanting go-away hawk", "Kakoshi", "Birds"),
        Entry("Pale harrier", "Kakoshi ka nika", "Birds"), Entry("Swallow", "Akamimbi", "Birds"), Entry("Francolin", "Akapeshi", "Birds"),
        Entry("Eagle", "Kapumpe", "Birds"), Entry("Small dove", "Akatutwa", "Birds"), Entry("Wagtail", "Akatyetye", "Birds"),
        Entry("Go-away bird", "Kuwe", "Birds"), Entry("Hornbill", "Ulukoma", "Birds"), Entry("Hawk", "Ulukoshi", "Birds"),
        Entry("Nightjar", "Ulumbasa", "Birds"), Entry("Spurwing goose", "Imbata", "Birds"), Entry("Bee-eater", "Milumbe", "Birds"),
        Entry("Speckled coly", "Milumbelumbe", "Birds"), Entry("Parrot", "Mucence", "Birds"), Entry("Pelican", "Mukanga", "Birds"),
        Entry("Roller", "Mukufi", "Birds"), Entry("Coucal", "Mukuta", "Birds"), Entry("Long-tailed widow bird", "Muleya", "Birds"),
        Entry("Kingfisher", "Mulowa", "Birds"), Entry("Drongo", "Mutengwe", "Birds"), Entry("Ostrich", "Mwakatala", "Birds"),
        Entry("Pied raven", "Mwankole", "Birds"), Entry("Long-legged k...", "Namungwa", "Birds"), Entry("Crested crane", "Ngoli", "Birds"),
        Entry("Heron / tickbird", "Nkooba", "Birds"), Entry("Green pigeon", "Nkondonkondo", "Birds"), Entry("Red-necked francolin", "Nkwale", "Birds"),
        Entry("Waxbill", "Nseba", "Birds"), Entry("Honey bird", "Nsolo", "Birds"), Entry("Honey bird", "Luuni", "Birds"),
        Entry("Falcon", "Pungwa", "Birds"), Entry("Sunbird", "Sosa", "Birds"), Entry("Small warbler", "Tiiti", "Birds"), Entry("Woodpecker", "Tondwe", "Birds"),

        Entry("Erythrophleum tree", "Kaimbi", "Trees"), Entry("Carpentry tree", "Mululu", "Trees"),
        Entry("Entandrophragma delevoyi", "Mofu", "Trees"), Entry("Pterocarpus angolensis", "Mulombwa", "Trees"),
        Entry("Afzelia quanzensis", "Mupapa", "Trees"), Entry("Parinarium mobola", "Mupundu", "Trees"), Entry("Faurea speciosa", "Saninga", "Trees"),
        Entry("Afromosia angolensis", "Mubanga", "Trees"), Entry("Monotes oblongifolius", "Cipampa", "Trees"), Entry("Syzygium", "Lwamba", "Trees"),
        Entry("Diospyros", "Mucenja", "Trees"), Entry("Hirtella bangweolensis", "Mukuwe", "Trees"), Entry("Marquesia macroura", "Museshi", "Trees"),
        Entry("Xylopia", "Mwengele", "Trees"), Entry("Albizzia sericocephala", "Musase", "Trees"), Entry("Barlinia craibiana", "Mutobo", "Trees"),
        Entry("Erythrina abyssinica", "Mulunguti", "Trees"), Entry("Swartzia madagascariensis", "Ndale", "Trees"), Entry("Dalbergia nitidula", "Kalongwe", "Trees"),
        Entry("Brachystegia allenii", "Mutondo", "Trees"), Entry("Unknown tree", "Mpaasa", "Trees"), Entry("Unknown tree", "Ciya", "Trees"),
        Entry("Brachystegia longifolia", "Muombo", "Trees"), Entry("Taxifolia", "Ngalati", "Trees"), Entry("Microphylla", "Mushike", "Trees"), Entry("Speciformis", "Muputu", "Trees"),

        Entry("Fruit tree", "Mupundu", "Fruit Trees", "Parinarium mobola"), Entry("Musuku tree", "Musuku", "Fruit Trees", "Uapaca"),
        Entry("Mufungo tree", "Mufungo", "Fruit Trees", "Anisophyllea pomifera"), Entry("Mukunyu tree", "Mukunyu", "Fruit Trees", "Ficus gr..."),
        Entry("Musafwa tree", "Musafwa", "Fruit Trees", "Syzygium"), Entry("Mukome tree", "Mukome", "Fruit Trees", "Strychnos"),
        Entry("Mulebe tree", "Mulebe", "Fruit Trees", "Strophanthus"), Entry("Muteke tree", "Muteke", "Fruit Trees", "Landolphia parvifolia"),
        Entry("Musongole tree", "Musongole", "Fruit Trees", "Strychnos cocculoides"), Entry("Black musokolobe", "Musokolobe wafita", "Fruit Trees", "Uapaca nitida"),
        Entry("White musokolobe", "Musokolobe wabuuta", "Fruit Trees"), Entry("Muminu", "Muminu", "Fruit Trees"), Entry("Mukole", "Mukole", "Fruit Trees"),
        Entry("Mungolomya", "Mungolomya", "Fruit Trees"), Entry("Mango tree", "Umuyembe", "Fruit Trees", "Fruit: yembe"),
        Entry("Avocado tree", "Umukotapeela", "Fruit Trees", "Fruit: kotapeela"),

        // Corrected greetings and useful expressions
        Entry("Hello / Hi", "Mwapola / Mwapoleni", "Greetings", "Use the form appropriate to context."),
        Entry("How are you?", "Uli shani? / Muli shani?", "Greetings", "Uli shani? informal; Muli shani? formal or plural."),
        Entry("Good morning", "Mwashibukeni!", "Greetings", "Responses include Eya mukwai, Endi, Endita mukwai."),
        Entry("What is the news?", "Kuli ci?", "Greetings", "Response: Kwatalala."),
        Entry("Good afternoon", "Kasuba mukwai", "Greetings"), Entry("Good evening", "Chungulo mukwai", "Greetings"),
        Entry("Good night", "Sendameenipo", "Greetings"), Entry("Sleep well", "Ulale umutende / Ulale bwino", "Greetings", "Formal: Mulale umutende / Mulale bwino."),
        Entry("Welcome", "Mwaiseni", "Greetings"), Entry("Goodbye / Fare well", "Kafikenipo / Shaleenipo", "Greetings"),
        Entry("Fare well", "Wende umutende / Mwende umutende", "Greetings", "Informal / formal."),
        Entry("Condolences", "Mwalosheni mukwai / Mwaculeni mukwai", "Greetings"),
        Entry("Are you eating well?", "Mwalileni / Mwalyeni bwino", "Greetings", "Response: Kulila mulelya."),
        Entry("Greetings to one at work", "Mwabombeni", "Greetings"),
        Entry("Greetings to a returning hunter", "Mwabambeni / Mabingo / Icibamfi", "Greetings"),
        Entry("Greetings to returning army / after killing dangerous animal", "Mwasalipeni", "Greetings"),
        Entry("Greetings to one who escaped danger", "Mwapusukeni", "Greetings"),
        Entry("Greeting to a Chief when leaving", "Lwapakata Mukwai", "Greetings"),

        // Family and relationships
        Entry("Father", "Tata", "Family"), Entry("My father", "Ba Tata", "Family"), Entry("Our father", "Shifwe", "Family"),
        Entry("Your father", "Wiso", "Family", "Formal: Ba Wiso; plural/formal: Shinwe."), Entry("His / her father", "Wishi", "Family", "Formal: Ba wishi."), Entry("Their father", "Shibo", "Family"),
        Entry("Mother", "Mayo", "Family", "Also Ba mayo."), Entry("Our mother", "Nyinefwe", "Family"),
        Entry("Your mother", "Noko", "Family", "Formal: Ba Noko; plural: Nyinenwe / Ba Nyinenwe."), Entry("His / her mother", "Nyina", "Family", "Formal: Ba Nyina."), Entry("Their mother", "Nyinabo", "Family"),
        Entry("Father-in-law", "Tatafyala", "Family"), Entry("Your father-in-law", "Sofyala", "Family"), Entry("His / her father-in-law", "Shifyala", "Family"), Entry("Our father-in-law", "Shifyalefwe", "Family"), Entry("Your father-in-law (plural)", "Shifyalenwe", "Family"), Entry("Their father-in-law", "Shifyalabo", "Family"),
        Entry("Mother-in-law", "Mayofyala / Mamafyala", "Family"), Entry("Our mother-in-law", "Nafyalefwe", "Family"), Entry("Your mother-in-law", "Nokofyala", "Family"), Entry("His / her mother-in-law", "Nyinafyala", "Family"),
        Entry("Grandfather", "Shikulu", "Family"), Entry("Our grandfather", "Shikulwifwe", "Family"), Entry("Your grandfather", "Sokulu / Shikulwinwe", "Family"), Entry("His / her grandfather", "Shiikulu / Shikulwibo", "Family"),
        Entry("Son / Daughter", "Mwana mwaume", "Family"), Entry("My son / daughter", "Mwana wandi", "Family"), Entry("Your son / daughter", "Mwana obe", "Family"), Entry("His / her son / daughter", "Mwana wakwe", "Family"), Entry("Our son / daughter", "Mwana wesu", "Family"), Entry("Their son / daughter", "Mwana wabo", "Family"), Entry("Your son / daughter (plural)", "Mwana wenu", "Family"),
        Entry("Brother", "Ndume nandi", "Family"), Entry("Your brother", "Ndume nobe / Ndume yobe", "Family"), Entry("His / her brother", "Ndume nankwe", "Family"), Entry("Our brother", "Ndume nensu", "Family"),
        Entry("Sister", "Nkashi", "Family"), Entry("My sister", "Nkashi nandi", "Family"), Entry("Your sister", "Nkashi yobe", "Family"), Entry("His / her sister", "Nkashi yakhe", "Family"),
        Entry("Husband", "Mûlume", "Family"), Entry("My husband", "Mwiina mwandi / Mûlume wandi", "Family"), Entry("Your husband", "Mwina mobe / Mulume obe", "Family"), Entry("Her husband", "Mwina mwakwe", "Family"),
        Entry("Wife", "Mûkashi", "Family", "Formal: Bâkashi."), Entry("My wife", "Mukashi wandi", "Family", "Formal: Bakashi bandi."), Entry("Your wife", "Mukashi obe", "Family", "Formal: Bakashi bone."), Entry("His wife", "Mukashi wakwe", "Family", "Formal: Bakashi bakwe."), Entry("Their wives", "Bakashi babo", "Family"),
        Entry("Paternal uncle", "Tata mwaice", "Family", "Formal: Ba Tata mwaice."), Entry("Your paternal uncle", "Wiso mwaice", "Family", "Formal: Ba Wiso mwaice; plural: Shinwe mwaice."), Entry("His / her paternal uncle", "Wishi mwaice", "Family", "Formal: Ba wishi mwaice."), Entry("Our paternal uncle", "Shifwe mwaice", "Family"),
        Entry("Maternal uncle", "Yama / Nalumefwe", "Family"), Entry("Your maternal uncle", "Nokolume / Nalumenwe", "Family"), Entry("His / her maternal uncle", "Nalume / Nalumebo", "Family"), Entry("Our maternal uncle", "Nalumefwe", "Family"),
        Entry("Paternal aunt", "Mayosenge", "Family", "Formal: Ba mayo senge."), Entry("Your paternal aunt", "Nokosenge", "Family", "Formal: Ba nokosenge."), Entry("His / her paternal aunt", "Nasenge", "Family", "Formal: Ba nasenge."), Entry("Their paternal aunt", "Nasengebo", "Family"), Entry("Our paternal aunt", "Nasengefwe", "Family"),
        Entry("Maternal aunt", "Mayo mwaice", "Family", "Formal: Ba mayo mwaice."), Entry("Your maternal aunt", "Noko mwaice", "Family", "Formal: Ba noko mwaice."), Entry("His / her maternal aunt", "Nyina mwaice", "Family", "Formal: Ba nyina mwaice."), Entry("Our maternal aunt", "Nyinefwe mwaice", "Family"), Entry("Their maternal aunt", "Nyinabo mwaice", "Family"),

        // Grammar reference entries
        Entry("I", "ine", "Pronouns", "1st person singular absolute personal pronoun."), Entry("You", "iwe", "Pronouns", "2nd person singular absolute personal pronoun."), Entry("We", "ifwe", "Pronouns", "1st person plural absolute personal pronoun."), Entry("You (plural)", "imwe", "Pronouns", "2nd person plural absolute personal pronoun."),
        Entry("Subject prefix: I", "n-", "Grammar", "1st person singular affirmative."), Entry("Subject prefix: you", "u-", "Grammar", "2nd person singular affirmative."), Entry("Subject prefix: he/she", "a-", "Grammar", "3rd person singular affirmative."), Entry("Subject prefix: we", "tu-", "Grammar", "1st person plural affirmative."),
        Entry("Object prefix: you", "ku-", "Grammar", "2nd person singular object prefix."), Entry("Object prefix: him/her", "mu-", "Grammar", "3rd person singular object prefix."), Entry("Object prefix: us", "tu-", "Grammar", "1st person plural object prefix."),
        Entry("Subjunctive", "-e", "Grammar", "Commonly changes the final -a of a verb to -e."), Entry("Simple imperative", "verb stem + -a", "Grammar", "Plural imperative changes -a to -eni."),
        Entry("Infinitive", "ku- + verb", "Grammar", "The infinitive is a verbal noun; habitual infinitive uses kula-."),
        Entry("Adjective concord", "adjective follows noun", "Grammar", "Adjectives follow the nouns they qualify and take concord prefixes."),
        Entry("Nominal prefix", "noun prefix", "Grammar"), Entry("Verbal prefix", "verb prefix", "Grammar"), Entry("Concord", "prefix harmony", "Grammar", "Harmony between prefixes in a sentence."),

        Entry("Ask", "Ukwipusha", "Verbs"), Entry("Engage (male)", "Ukukobekela", "Verbs"), Entry("Bemba language", "iciBemba", "Basic"),
        Entry("And / with", "na", "Conjunctions"), Entry("Like / as", "nga", "Conjunctions"), Entry("Morning", "uluceelo", "Basic"), Entry("A lot", "saana", "Basic"), Entry("Good", "suma", "Adjectives"), Entry("All", "onse", "Adjectives")
    )

    private val conversations = listOf(
        Conversation(listOf("how are you", "how r you", "how are u", "uli shani", "muli shani"), "Uli shani?", "How are you?", "Ndifye bwino."),
        Conversation(listOf("good morning", "morning"), "Mwashibukeni!", "Good morning!", "Eya mukwai."),
        Conversation(listOf("good afternoon", "afternoon"), "Kasuba mukwai.", "Good afternoon.", "Endita mukwai."),
        Conversation(listOf("good evening", "evening"), "Chungulo mukwai.", "Good evening.", "Endita mukwai."),
        Conversation(listOf("good night", "night"), "Sendameenipo.", "Good night.", "Eya mukwai."),
        Conversation(listOf("thank you", "thanks", "thank u"), "Natotela.", "Thank you.", "Natotela saana."),
        Conversation(listOf("welcome"), "Mwaiseni.", "Welcome.", "Endita mukwai."),
        Conversation(listOf("i dont know", "i don't know", "i do not know"), "Nshishibe.", "I don't know."),
        Conversation(listOf("sorry", "forgive me", "pardon me"), "Njeleleneko.", "Sorry / Forgive me.", "Ndelomba ubwelelo."),
        Conversation(listOf("i love you", "love you"), "Nalikutemwa.", "I love you.", "Nalikutemwa sana."),
        Conversation(listOf("i miss you", "miss you"), "Nakufuluka.", "I miss you.", "Nakufuluka sana."),
        Conversation(listOf("help me", "help"), "Ngafwa / Ngafwako.", "Help me.", "Ngafweni / Ngafweniko."),
        Conversation(listOf("what is the news", "whats the news", "what's the news", "news"), "Kuli ci?", "What is the news?", "Kwatalala."),
        Conversation(listOf("where are you", "where r you", "where are u"), "Ulikwisa?", "Where are you?"),
        Conversation(listOf("where are they", "where r they"), "Balikwisa?", "Where are they?"),
        Conversation(listOf("i want money", "want money"), "Ndefwaya indalama.", "I want money."),
        Conversation(listOf("i am angry", "im angry", "i'm angry"), "Nimfulwa.", "I'm angry.")
    )

    private fun normalize(text: String): String = text.lowercase(Locale.ROOT)
        .replace("’", "'").replace(Regex("[^a-z0-9' ]"), " ")
        .replace(Regex("\\s+"), " ").trim()

    private fun smartConversation(query: String): Conversation? {
        val q = normalize(query)
        if (q.isBlank()) return null
        conversations.firstOrNull { c -> c.triggers.any { q == normalize(it) || q.contains(normalize(it)) } }?.let { return it }
        val words = q.split(" ").filter { it.length > 2 }.toSet()
        return conversations.map { c ->
            val score = c.triggers.maxOf { trigger ->
                val tw = normalize(trigger).split(" ").filter { it.length > 2 }.toSet()
                if (tw.isEmpty()) 0.0 else tw.intersect(words).size.toDouble() / tw.size
            }
            c to score
        }.filter { it.second >= 0.5 }.maxByOrNull { it.second }?.first
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favorites.addAll(getPreferences(0).getStringSet("favorites", emptySet()) ?: emptySet())
        tts = TextToSpeech(this, this)
        buildUi()
    }

    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale.UK }

    private fun rounded(color: Int, radius: Float = 22f, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
        if (stroke != null) setStroke(1, stroke)
    }

    private fun buildUi() {
        val bg = Color.rgb(246, 243, 234)
        val green = Color.rgb(8, 66, 49)
        val gold = Color.rgb(190, 150, 58)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(22, 24, 22, 20); setBackgroundColor(green)
        }
        val titleRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        titleRow.addView(TextView(this).apply {
            text = "BembaTranslate"; textSize = 25f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, -2, 1f))
        titleRow.addView(TextView(this).apply {
            text = "OFFLINE"; textSize = 9f; setTextColor(green); gravity = Gravity.CENTER
            setPadding(12, 6, 12, 6); background = rounded(Color.rgb(222, 202, 143), 18f)
        })
        header.addView(titleRow)
        header.addView(TextView(this).apply {
            text = "English ↔ Bemba  •  Dictionary & conversation"
            textSize = 12f; setTextColor(Color.rgb(225, 210, 165)); setPadding(0, 6, 0, 0)
        })
        root.addView(header)

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(14, 12, 14, 8); setBackgroundColor(Color.WHITE) }
        val translateBtn = navButton("TRANSLATE", true, green, gold)
        val dictBtn = navButton("DICTIONARY", false, green, gold)
        nav.addView(translateBtn, LinearLayout.LayoutParams(0, 46, 1f).apply { setMargins(0,0,5,0) })
        nav.addView(dictBtn, LinearLayout.LayoutParams(0, 46, 1f).apply { setMargins(5,0,0,0) })
        root.addView(nav)

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 10, 18, 12) }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        translateBtn.setOnClickListener { showingDictionary = false; styleNav(translateBtn, dictBtn, true, false, green); renderHome() }
        dictBtn.setOnClickListener { showingDictionary = true; styleNav(translateBtn, dictBtn, false, true, green); renderDictionary() }
        renderHome()
    }

    private fun navButton(text: String, selected: Boolean, green: Int, gold: Int) = TextView(this).apply {
        this.text = text; textSize = 11f; gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (selected) Color.WHITE else green)
        background = rounded(if (selected) green else Color.rgb(238,235,225), 16f)
    }

    private fun styleNav(a: TextView, b: TextView, asel: Boolean, bsel: Boolean, green: Int) {
        a.setTextColor(if (asel) Color.WHITE else green); a.background = rounded(if (asel) green else Color.rgb(238,235,225), 16f)
        b.setTextColor(if (bsel) Color.WHITE else green); b.background = rounded(if (bsel) green else Color.rgb(238,235,225), 16f)
    }

    private fun renderHome() {
        content.removeAllViews()
        val green = Color.rgb(8, 66, 49)
        val gold = Color.rgb(190, 150, 58)
        content.addView(TextView(this).apply {
            text = "Translate"; textSize = 23f; setTextColor(green); typeface = Typeface.DEFAULT_BOLD
        })
        content.addView(TextView(this).apply {
            text = "Type naturally. Get a clear Bemba answer instantly — no internet required."
            textSize = 12f; setTextColor(Color.DKGRAY); setPadding(0, 3, 0, 10)
        })

        val direction = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(10, 5, 10, 5)
            background = rounded(Color.WHITE, 18f)
        }
        val from = TextView(this).apply { textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setTextColor(green); gravity = Gravity.CENTER }
        val swap = TextView(this).apply { text = "⇄"; textSize = 24f; setTextColor(gold); gravity = Gravity.CENTER; setPadding(16,0,16,0) }
        val to = TextView(this).apply { textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setTextColor(green); gravity = Gravity.CENTER }
        direction.addView(from, LinearLayout.LayoutParams(0,40,1f)); direction.addView(swap, LinearLayout.LayoutParams(-2,40)); direction.addView(to, LinearLayout.LayoutParams(0,40,1f))
        content.addView(direction, LinearLayout.LayoutParams(-1,52).apply { setMargins(0,2,0,8) })

        val input = EditText(this).apply {
            hint = "Type English…"; textSize = 17f; setSingleLine(false); minLines = 2; gravity = Gravity.TOP
            setPadding(17,14,17,14); background = rounded(Color.WHITE, 20f, Color.rgb(225,220,208)); setHintTextColor(Color.GRAY)
        }
        content.addView(input, LinearLayout.LayoutParams(-1,96).apply { setMargins(0,0,0,6) })

        val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val clear = TextView(this).apply { text = "CLEAR"; textSize=10f; setTextColor(green); gravity=Gravity.CENTER; typeface=Typeface.DEFAULT_BOLD }
        val speaker = TextView(this).apply { text = "🔊  SPEAK"; textSize=10f; setTextColor(green); gravity=Gravity.CENTER; typeface=Typeface.DEFAULT_BOLD }
        actionRow.addView(clear, LinearLayout.LayoutParams(0,42,1f).apply { setMargins(0,0,4,0) })
        actionRow.addView(speaker, LinearLayout.LayoutParams(0,42,1f).apply { setMargins(4,0,0,0) })
        content.addView(actionRow, LinearLayout.LayoutParams(-1,42).apply { setMargins(0,0,0,5) })

        val resultScroll = ScrollView(this)
        val result = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        resultScroll.addView(result)
        content.addView(resultScroll, LinearLayout.LayoutParams(-1,0,1f))

        fun refreshLabels() {
            from.text = if (englishToBemba) "ENGLISH" else "BEMBA"
            to.text = if (englishToBemba) "BEMBA" else "ENGLISH"
            input.hint = if (englishToBemba) "Type English…" else "Type Bemba…"
            renderResult(input.text.toString(), result)
        }
        swap.setOnClickListener { englishToBemba = !englishToBemba; refreshLabels(); input.requestFocus() }
        clear.setOnClickListener { input.text.clear() }
        speaker.setOnClickListener {
            val tv = result.findViewWithTag<TextView>("bemba_result")
            if (tv != null) speak(tv.text.toString().trim())
        }
        input.addTextChangedListener(SimpleTextWatcher { q -> renderResult(q, result) })
        refreshLabels()
    }

    private fun renderResult(query: String, result: LinearLayout) {
        result.removeAllViews()
        if (query.isBlank()) {
            result.addView(sectionTitle(if (history.isEmpty()) "Quick phrases" else "Recent translations"))
            val items = if (history.isEmpty()) listOf("How are you?", "Good morning", "I want money", "Where are you?", "Where are they?", "I'm angry") else history.take(6)
            items.forEach { q ->
                val e = entries.find { it.english.equals(q, true) || it.bemba.equals(q, true) }
                if (e != null) addCard(result, e)
            }
            return
        }
        val smart = if (englishToBemba) smartConversation(query) else null
        if (smart != null) {
            addConversationCard(result, smart)
            return
        }
        val q = query.trim().lowercase()
        val hits = if (englishToBemba) {
            entries.filter { it.english.lowercase().contains(q) }
        } else {
            entries.filter { it.bemba.lowercase().contains(q) }
        }
        if (hits.isEmpty()) {
            result.addView(TextView(this).apply {
                text = "No translation found yet. Try another word or open Dictionary."
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(4,18,4,10)
            })
            return
        }
        hits.take(8).forEach { e ->
            if (!history.contains(e.english)) { history.add(0, e.english); if (history.size > 12) history.removeAt(history.lastIndex) }
            addTranslationCard(result, e)
        }
    }

    private fun addConversationCard(parent: LinearLayout, c: Conversation) {
        val green = Color.rgb(8,66,49); val gold = Color.rgb(190,150,58)
        val card = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,18,16,14); background=rounded(Color.WHITE,22f) }
        card.addView(TextView(this).apply { text="NATURAL BEMBA RESPONSE"; textSize=9f; setTextColor(gold); typeface=Typeface.DEFAULT_BOLD })
        card.addView(TextView(this).apply { tag="bemba_result"; text=c.bemba; textSize=26f; setTextColor(green); typeface=Typeface.DEFAULT_BOLD; setPadding(0,7,0,2) })
        if(c.englishReply.isNotBlank()) card.addView(TextView(this).apply { text=c.englishReply; textSize=12f; setTextColor(Color.DKGRAY) })
        if(c.suggestion.isNotBlank()) card.addView(TextView(this).apply { text="Suggested reply  •  ${c.suggestion}"; textSize=12f; setTextColor(gold); typeface=Typeface.DEFAULT_BOLD; setPadding(0,10,0,0) })
        val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.RIGHT}
        actions.addView(TextView(this).apply{text="COPY";textSize=10f;setTextColor(green);typeface=Typeface.DEFAULT_BOLD;setPadding(12,12,12,0);setOnClickListener{copyText(c.bemba)}})
        actions.addView(TextView(this).apply{text="🔊";textSize=18f;setPadding(12,7,4,0);setOnClickListener{speak(c.bemba)}})
        card.addView(actions); parent.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,10)})
        if(!history.contains(c.englishReply)) { history.add(0,c.englishReply); if(history.size>12)history.removeAt(history.lastIndex) }
    }

    private fun addTranslationCard(parent: LinearLayout, e: Entry) {
        val green=Color.rgb(8,66,49)
        val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,16,16,14);background=rounded(Color.WHITE,22f)}
        val label=TextView(this).apply{text=if(englishToBemba)"BEMBA TRANSLATION" else "ENGLISH MEANING";textSize=9f;setTextColor(Color.GRAY);typeface=Typeface.DEFAULT_BOLD}
        val value=TextView(this).apply{tag="bemba_result";text=if(englishToBemba)e.bemba else e.english;textSize=25f;setTextColor(green);typeface=Typeface.DEFAULT_BOLD;setPadding(0,6,0,3)}
        val source=TextView(this).apply{text=if(englishToBemba)e.english else "${e.bemba}  •  ${e.category}";textSize=12f;setTextColor(Color.DKGRAY)}
        card.addView(label);card.addView(value);card.addView(source)
        if(e.note.isNotBlank())card.addView(TextView(this).apply{text=e.note;textSize=11f;setTextColor(Color.GRAY);setPadding(0,6,0,0)})
        val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.RIGHT}
        actions.addView(TextView(this).apply{text="COPY";textSize=10f;setTextColor(green);typeface=Typeface.DEFAULT_BOLD;setPadding(12,10,12,0);setOnClickListener{copyText(if(englishToBemba)e.bemba else e.english)}})
        actions.addView(TextView(this).apply{text="🔊";textSize=18f;setPadding(12,5,4,0);setOnClickListener{speak(if(englishToBemba)e.bemba else e.english)}})
        card.addView(actions);parent.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,10)})
    }

    private fun renderDictionary() {
        content.removeAllViews()
        val green=Color.rgb(8,66,49); val gold=Color.rgb(190,150,58)
        content.addView(TextView(this).apply{text="Bemba Dictionary";textSize=23f;setTextColor(green);typeface=Typeface.DEFAULT_BOLD})
        content.addView(TextView(this).apply{text="Your growing offline Bemba word bank.";textSize=12f;setTextColor(Color.DKGRAY);setPadding(0,3,0,10)})
        search=EditText(this).apply{hint="Search English or Bemba…";singleLine=true;textSize=15f;setPadding(16,0,16,0);background=rounded(Color.WHITE,18f,Color.rgb(225,220,208));setHintTextColor(Color.GRAY)}
        content.addView(search,LinearLayout.LayoutParams(-1,54).apply{setMargins(0,0,0,2)})
        val categories=HorizontalScrollView(this).apply{isHorizontalScrollBarEnabled=false;setPadding(0,8,0,5)}
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        listOf("All","Greetings","Basic","Phrases","Animals","Family","People","Body","Adjectives","Numbers","Days","Months","Verbs","Conjunctions","Snakes","Birds","Trees","Fruit Trees","Pronouns","Grammar").forEach{c->
            row.addView(TextView(this).apply{text=c;textSize=10f;gravity=Gravity.CENTER;setPadding(14,8,14,8);setTextColor(if(c==selectedCategory)Color.WHITE:green);background=rounded(if(c==selectedCategory)green:Color.WHITE,16f);setOnClickListener{selectedCategory=c;renderDictionary()}},LinearLayout.LayoutParams(-2,38).apply{setMargins(0,0,6,0)})
        }
        categories.addView(row);content.addView(categories)
        val scroll=ScrollView(this);val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};scroll.addView(list);content.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        fun refresh(q:String){list.removeAllViews();find(q).filter{selectedCategory=="All"||it.category==selectedCategory}.take(100).forEach{addCard(list,it)}}
        search.addTextChangedListener(SimpleTextWatcher{refresh(it)});refresh("")
    }

    private fun find(q: String): List<Entry> { val x=q.trim().lowercase(); return entries.filter { x.isEmpty() || it.english.lowercase().contains(x) || it.bemba.lowercase().contains(x) || it.note.lowercase().contains(x) } }
    private fun sectionTitle(t:String)=TextView(this).apply{text=t;textSize=15f;setTextColor(Color.rgb(8,66,49));setTypeface(null,Typeface.BOLD);setPadding(0,0,0,8)}

    private fun addCard(parent: LinearLayout, e: Entry) {
        val green=Color.rgb(8,66,49); val gold=Color.rgb(190,150,58)
        val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,14,14,12);background=rounded(Color.WHITE,20f)}
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        top.addView(TextView(this).apply{text=e.english;textSize=15f;setTextColor(Color.DKGRAY);typeface=Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(0,-2,1f))
        top.addView(TextView(this).apply{text=if(favorites.contains(e.english))"★" else "☆";textSize=23f;setTextColor(gold);setOnClickListener{if(!favorites.add(e.english))favorites.remove(e.english);saveFavorites();text=if(favorites.contains(e.english))"★" else "☆"}})
        top.addView(TextView(this).apply{text="🔊";textSize=18f;setPadding(12,0,5,0);setOnClickListener{speak(e.bemba)}})
        card.addView(top)
        card.addView(TextView(this).apply{text="→  ${e.bemba}";textSize=20f;setTextColor(green);typeface=Typeface.DEFAULT_BOLD;setPadding(0,6,0,1)})
        if(e.note.isNotBlank())card.addView(TextView(this).apply{text=e.note;textSize=11f;setTextColor(Color.GRAY);setPadding(0,4,0,0)})
        card.addView(TextView(this).apply{text=e.category.uppercase();textSize=8f;setTextColor(gold);typeface=Typeface.DEFAULT_BOLD;setPadding(0,8,0,0)})
        parent.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,8)})
    }

    private fun speak(text:String){if(::tts.isInitialized)tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"bemba_${System.currentTimeMillis()}")}
    private fun copyText(text:String){(getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Bemba",text));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show()}
    private fun saveFavorites(){getPreferences(0).edit().putStringSet("favorites",favorites).apply()}
    override fun onDestroy(){if(::tts.isInitialized){tts.stop();tts.shutdown()};super.onDestroy()}
}

class SimpleTextWatcher(private val f:(String)->Unit):android.text.TextWatcher{
    override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
    override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){f(s?.toString() ?: "")}
    override fun afterTextChanged(s:android.text.Editable?){}
}
