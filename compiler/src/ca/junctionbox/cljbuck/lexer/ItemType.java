package ca.junctionbox.cljbuck.lexer;

public enum ItemType {
    itemError,
    itemEOF,
    itemBool,           // true, false
    itemChar,           // \a
    itemKeyword,        // :ack, ::ack, :rubber-baby-buggy-bumper!, :j3_!:7,
    itemLeftBrace,      // {
    itemLeftBracket,    // [
    itemLeftParen,      // (
    itemNil,
    itemNumber,         // 12345, 1235.00,
    itemRightBrace,     // }
    itemRightBracket,   // ]
    itemRightParen,     // )
    itemString,         // "Banana"
    itemVar, itemComment, itemSymbol, itemLong, itemHex, itemOctal, itemBase36, itemRightMap, itemLeftList, itemLeftMap, itemRightList, itemLeftVector, itemRightVector, itemText,
}
