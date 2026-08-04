import test from 'node:test'
import assert from 'node:assert/strict'
import {
    buildPrintWeeks,
    createDoctorPrintAbbreviationMap,
    getAssistantPrintNameClass,
    getDoctorPrintAbbreviation,
    getOfficePrintTeamCountClass,
    sortOfficePrintSchedules
} from './printScheduleUtils.js'

const requiredMonths = [
    { label: 'August 2026', year: 2026, monthIndex: 7, rows: 5 },
    { label: 'September 2026', year: 2026, monthIndex: 8, rows: 5 },
    { label: 'February 2027', year: 2027, monthIndex: 1, rows: 4 },
    { label: 'May 2027', year: 2027, monthIndex: 4, rows: 5 },
    { label: 'August 2027', year: 2027, monthIndex: 7, rows: 5 }
]

for (const { label, year, monthIndex, rows } of requiredMonths) {
    test(`${label} uses ${rows} printable calendar rows`, () => {
        const weeks = buildPrintWeeks(year, monthIndex)

        assert.equal(weeks.length, rows)
        assert.ok(weeks.every((week) => week.length === 4))
        assert.ok(weeks.every((week) => week[0].getDay() === 1))
        assert.ok(weeks.every((week) => week[3].getDay() === 4))
    })
}

test('doctor print abbreviations are compact and collision-aware', () => {
    const names = ['Collie', 'Macon', 'Lamb', 'McCutchen']
    const abbreviations = createDoctorPrintAbbreviationMap(names)

    assert.equal(getDoctorPrintAbbreviation('Collie', abbreviations), 'C')
    assert.equal(getDoctorPrintAbbreviation('Lamb', abbreviations), 'L')
    assert.equal(getDoctorPrintAbbreviation('Macon', abbreviations), 'M')
    assert.equal(getDoctorPrintAbbreviation('McCutchen', abbreviations), 'Mc')
})

test('August 2026 uses the trailing Tuesday through Thursday cells for print extras', () => {
    const weeks = buildPrintWeeks(2026, 7)
    const finalWeek = weeks.at(-1)

    assert.deepEqual(
        finalWeek.map((date) => [date.getMonth(), date.getDate()]),
        [[7, 31], [8, 1], [8, 2], [8, 3]]
    )
})

test('doctor abbreviation formatting removes an optional doctor prefix', () => {
    const abbreviations = createDoctorPrintAbbreviationMap(['Dr. Collie'])

    assert.equal(getDoctorPrintAbbreviation('Dr. Collie', abbreviations), 'C')
})

test('print offices are ordered Raleigh, Garner, Smithfield, then other offices', () => {
    const schedules = [
        { officeName: 'Smithfield', id: 3 },
        { officeName: 'North Hills', id: 4 },
        { officeName: 'Garner', id: 2 },
        { officeName: 'Raleigh', id: 1 }
    ]

    assert.deepEqual(
        sortOfficePrintSchedules(schedules).map((entry) => entry.officeName),
        ['Raleigh', 'Garner', 'Smithfield', 'North Hills']
    )
})

test('assistant print name classes scale individual long names only', () => {
    assert.equal(getAssistantPrintNameClass('Cori'), 'print-assistant-name')
    assert.equal(
        getAssistantPrintNameClass('Constance'),
        'print-assistant-name print-assistant-name-long'
    )
    assert.equal(
        getAssistantPrintNameClass('Alexandrianna'),
        'print-assistant-name print-assistant-name-very-long'
    )
})

test('office print width class reflects its doctor team count', () => {
    assert.equal(
        getOfficePrintTeamCountClass({ teams: { 1: [] } }),
        'print-office-team-count-1'
    )
    assert.equal(
        getOfficePrintTeamCountClass({ teams: { 1: [], 2: [] } }),
        'print-office-team-count-2'
    )
    assert.equal(
        getOfficePrintTeamCountClass({ teams: { 1: [], 2: [], 3: [], 4: [], 5: [] } }),
        'print-office-team-count-4'
    )
})
