val text = """Health & Wellness Card
Policy No : OG-27-1102-8403-00000041
Valid Upto : 16-AUG-2027
Name : HERISH KUMAR
Gender : MALE
Date of Birth : 27-SEP-2005
Age : 20 Years
ID Card No : GMC-27110230041-20711
Company Name : APPLIED RESEARCH INTERNATIONAL PVT LTD"""

val policyRegex = Regex("""(?:Policy|Invoice|Card|ID|Receipt)[\s\w]*?(?:No|Number|#)[\s:.-]*([A-Z0-9/-]+)""", RegexOption.IGNORE_CASE)
val match = policyRegex.find(text)
println("Doc Number match: " + match?.groupValues?.get(1))
