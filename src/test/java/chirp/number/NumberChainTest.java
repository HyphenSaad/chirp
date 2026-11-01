package chirp.number;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberChainTest {

    // ============================================================== factories

    @Test
    void ofInt() {
        assertEquals(5.0, NumberChain.of(5).asDouble(), 0.001);
    }

    @Test
    void ofDouble() {
        assertEquals(3.14, NumberChain.of(3.14).asDouble(), 0.001);
    }

    @Test
    void factoryAliases() {
        assertEquals(5, Numbers.of(5).asInt());
        assertEquals(5, Numbers.chain(5).asInt());
        assertEquals(5, Numbers.from(5).asInt());
    }

    // ============================================================== arithmetic

    @Test
    void add() {
        assertEquals(8, NumberChain.of(5).add(3).asInt());
        assertEquals(8, NumberChain.of(5).plus(3).asInt());
        assertEquals(8, NumberChain.of(5).sum(3).asInt());
        assertEquals(8, NumberChain.of(5).and(3).asInt());
    }

    @Test
    void subtract() {
        assertEquals(2, NumberChain.of(5).subtract(3).asInt());
        assertEquals(2, NumberChain.of(5).minus(3).asInt());
        assertEquals(2, NumberChain.of(5).sub(3).asInt());
        assertEquals(2, NumberChain.of(5).take(3).asInt());
    }

    @Test
    void multiply() {
        assertEquals(15, NumberChain.of(5).multiply(3).asInt());
        assertEquals(15, NumberChain.of(5).times(3).asInt());
        assertEquals(15, NumberChain.of(5).mul(3).asInt());
    }

    @Test
    void divide() {
        assertEquals(2, NumberChain.of(10).divide(5).asInt());
        assertEquals(2, NumberChain.of(10).div(5).asInt());
        assertEquals(2, NumberChain.of(10).dividedBy(5).asInt());
        assertEquals(2, NumberChain.of(10).over(5).asInt());
    }

    @Test
    void divideByZeroReturnsZero() {
        assertEquals(0, NumberChain.of(10).divide(0).asInt());
        assertEquals(0, NumberChain.of(10).divide(0.0).asInt());
    }

    @Test
    void mod() {
        assertEquals(1, NumberChain.of(7).mod(3).asInt());
        assertEquals(1, NumberChain.of(7).modulo(3).asInt());
        assertEquals(1, NumberChain.of(7).rem(3).asInt());
        assertEquals(7, NumberChain.of(7).mod(0).asInt());   // mod 0 leaves unchanged
    }

    @Test
    void pow() {
        assertEquals(8, NumberChain.of(2).pow(3).asInt());
        assertEquals(8, NumberChain.of(2).power(3).asInt());
        assertEquals(8, NumberChain.of(2).raisedTo(3).asInt());
    }

    // ============================================================== transforms

    @Test
    void abs() {
        assertEquals(5, NumberChain.of(-5).abs().asInt());
        assertEquals(5, NumberChain.of(-5).absolute().asInt());
    }

    @Test
    void negate() {
        assertEquals(-5, NumberChain.of(5).negate().asInt());
        assertEquals(-5, NumberChain.of(5).negative().asInt());
        assertEquals(-5, NumberChain.of(5).flipSign().asInt());
    }

    @Test
    void sqrt() {
        assertEquals(3.0, NumberChain.of(9).sqrt().asDouble(), 0.001);
        assertEquals(3.0, NumberChain.of(9).squareRoot().asDouble(), 0.001);
    }

    @Test
    void round() {
        assertEquals(4, NumberChain.of(3.6).round().asInt());
        assertEquals(4, NumberChain.of(3.6).rounded().asInt());
        assertEquals(3.14, NumberChain.of(3.14159).round(2).asDouble(), 0.001);
    }

    @Test
    void floorCeil() {
        assertEquals(3, NumberChain.of(3.9).floor().asInt());
        assertEquals(4, NumberChain.of(3.1).ceil().asInt());
        assertEquals(3, NumberChain.of(3.9).truncate().asInt());
    }

    @Test
    void clamp() {
        assertEquals(5, NumberChain.of(10).clamp(0, 5).asInt());
        assertEquals(0, NumberChain.of(-5).clamp(0, 5).asInt());
        assertEquals(3, NumberChain.of(3).clamp(0, 5).asInt());
        assertEquals(5, NumberChain.of(10).limit(0, 5).asInt());
        assertEquals(5, NumberChain.of(10).bound(0, 5).asInt());
    }

    @Test
    void minMax() {
        assertEquals(5, NumberChain.of(5).min(10).asInt());
        assertEquals(10, NumberChain.of(5).max(10).asInt());
    }

    @Test
    void applyFunction() {
        assertEquals(10, NumberChain.of(5).apply(x -> x * 2).asInt());
        assertEquals(10, NumberChain.of(5).map(x -> x * 2).asInt());
        assertEquals(10, NumberChain.of(5).then(x -> x * 2).asInt());
    }

    @Test
    void nullOperatorIgnored() {
        assertEquals(5, NumberChain.of(5).apply(null).asInt());
    }

    // ============================================================== terminals

    @Test
    void terminalAliases() {
        NumberChain c = NumberChain.of(42);
        assertEquals(42.0, c.asDouble(), 0.001);
        assertEquals(42.0, c.value(), 0.001);
        assertEquals(42.0, c.result(), 0.001);
        assertEquals(42.0, c.get(), 0.001);
        assertEquals(42, c.asInt());
        assertEquals(42, c.intValue());
        assertEquals(42, c.toInt());
        assertEquals(42L, c.asLong());
        assertEquals(42L, c.longValue());
        assertEquals(42L, c.toLong());
    }

    // ============================================================== formatted terminals

    @Test
    void formatted() {
        assertEquals("3.14", NumberChain.of(3.14159).formatted());
        assertEquals("3.1", NumberChain.of(3.14159).formatted(1));
        assertEquals("3.14", NumberChain.of(3.14159).asString());
    }

    @Test
    void toOrdinal() {
        assertEquals("1st", NumberChain.of(1).toOrdinal());
        assertEquals("5th", NumberChain.of(5).toOrdinal());
    }

    @Test
    void toRoman() {
        assertEquals("IV", NumberChain.of(4).toRoman());
    }

    @Test
    void toThousands() {
        assertEquals("1,234", NumberChain.of(1234).toThousands());
    }

    @Test
    void toBinaryAndHex() {
        assertEquals("1111", NumberChain.of(15).toBinary());
        assertEquals("f", NumberChain.of(15).toHex());
    }

    @Test
    void toAbbreviated() {
        assertEquals("1.5K", NumberChain.of(1500).toAbbreviated());
    }

    // ============================================================== predicates

    @Test
    void predicates() {
        assertTrue(NumberChain.of(4).isEven());
        assertTrue(NumberChain.of(3).isOdd());
        assertTrue(NumberChain.of(5).isPositive());
        assertTrue(NumberChain.of(-5).isNegative());
        assertTrue(NumberChain.of(0).isZero());
        assertTrue(NumberChain.of(7).isPrime());
        assertTrue(NumberChain.of(42).isInteger());
        assertFalse(NumberChain.of(42.5).isInteger());
    }

    // ============================================================== chaining demos

    @Test
    void longChain() {
        int result = NumberChain.of(10)
                .add(5)          // 15
                .times(2)        // 30
                .subtract(10)    // 20
                .divide(4)       // 5
                .pow(2)          // 25
                .sqrt()          // 5
                .asInt();
        assertEquals(5, result);
    }

    @Test
    void clampInLongChain() {
        int result = Numbers.of(10).add(5).times(2).clamp(0, 20).asInt();
        assertEquals(20, result);
    }

    @Test
    void immutabilityOfChain() {
        NumberChain base = NumberChain.of(5);
        NumberChain doubled = base.times(2);
        assertEquals(5, base.asInt());
        assertEquals(10, doubled.asInt());
    }
}
