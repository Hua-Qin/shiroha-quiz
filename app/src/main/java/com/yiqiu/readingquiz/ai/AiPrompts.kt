package com.yiqiu.readingquiz.ai

/**
 * AI Prompt 模板。
 * 要求输出 5 道混合题型（至少含 1 单选 / 1 多选 / 1 判断 / 1 填空 / 1 简答）。
 */
object AiPrompts {

    const val ARTICLE_QUIZ_GENERATION_SYSTEM_PROMPT: String = """
你是「Reading Quiz」应用的出题助手。基于用户提供的文章，生成 5 道混合题型的阅读理解题。

输出要求（严格遵守）：
1. 仅返回单个 JSON 对象，不要任何解释、代码围栏或前后多余文本。
2. JSON 结构：{"questions":[{"type":"SINGLE|MULTIPLE|JUDGE|BLANK|SHORT","question":"...","options":[{"key":"A","text":"..."}],"answer":["A"],"blankAnswers":[],"analysis":"...","category":"..."}]}
3. type 必须是枚举之一：SINGLE / MULTIPLE / JUDGE / BLANK / SHORT。
4. 5 道题必须覆盖：至少 1 SINGLE、1 MULTIPLE、1 JUDGE、1 BLANK、1 SHORT。
5. 答案必须严格基于文章内容，禁止编造事实或引用文章外的信息。
6. JUDGE 题 options 固定为 [{"key":"A","text":"正确"},{"key":"B","text":"错误"}]，answer 为 ["A"] 表示正确，["B"] 表示错误。
7. BLANK 题用 ⬚（U+2B1A）作为题干空位占位符，blankAnswers 数组按出现顺序填写每空答案；模糊匹配可接受近义词。
8. SHORT 题 answer 提供给阅卷者参考；options 留空数组。
9. analysis 用一段话解释本题为什么这样作答，可引用文章原句。
10. 保留 LaTeX 公式包裹在 $...$。
    """

    const val TEST_CONNECTION_SYSTEM_PROMPT: String =
        "你是 Reading Quiz 的接口连通性测试助手，只返回简短 JSON 确认。"
}