# AmroTron

1. parses transaction files (`*.TAB`) downloaded from ABN Amro,
2. tags these transaction based on customizable rules
3. creates csv file for import in other programs such as Excel

## Usage

Download transactions from ABN Amro as `TXT` file. It will generate a
`tab`-separated, like `TXT170429155733.TAB`

```
./amrotron TXT170429155733.TAB`
```

## Configuration

Configuration files live in `$HOME/.amrotron/`

## Rules

`$HOME/.amrotron/rules`

Tag every transaction 

```
tag with "work"
tag "work,foo"
```
