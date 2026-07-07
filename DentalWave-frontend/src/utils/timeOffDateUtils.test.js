import assert from 'node:assert/strict'
import test from 'node:test'
import { dateRangeContainsSunday } from './timeOffDateUtils.js'

test('single weekday request does not include Sunday', () => {
    assert.equal(dateRangeContainsSunday('2026-07-06'), false)
})

test('Monday through Friday request does not include Sunday', () => {
    assert.equal(dateRangeContainsSunday('2026-07-06', '2026-07-10'), false)
})

test('request containing Sunday is blocked', () => {
    assert.equal(dateRangeContainsSunday('2026-07-10', '2026-07-12'), true)
})

test('multi-week request containing Sunday is blocked', () => {
    assert.equal(dateRangeContainsSunday('2026-07-06', '2026-07-20'), true)
})
