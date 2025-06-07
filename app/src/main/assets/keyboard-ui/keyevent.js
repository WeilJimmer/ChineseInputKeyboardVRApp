// keyevent.js
// 處理鍵盤事件和注音輸入

var keyMode = 'Eng' // 預設為英文模式
var capLock = false; // Caps Lock 狀態
var symbolMode = false; // 符號模式
var halfWidth = true; // 半形狀態
var shiftPressed = false; // Shift 鍵狀態
var phoneticBuffer = [];
var candidateList = []; // 候選字列表
var nextPath = ['*']; // 下一個可能的路徑
const maxPhoneticLength = 4;

const fullWidthChars = {
    '1': '１', '2': '２', '3': '３', '4': '４', '5': '５',
    '6': '６', '7': '７', '8': '８', '9': '９', '0': '０',
    'Q': 'Ｑ', 'W': 'Ｗ', 'E': 'Ｅ', 'R': 'Ｒ', 'T': 'Ｔ',
    'q': 'ｑ', 'w': 'ｗ', 'e': 'ｅ', 'r': 'ｒ', 't': 'ｔ',
    'Y': 'Ｙ', 'U': 'Ｕ', 'I': 'Ｉ', 'O': 'Ｏ', 'P': 'Ｐ',
    'y': 'ｙ', 'u': 'ｕ', 'i': 'ｉ', 'o': 'ｏ', 'p': 'ｐ',
    'A': 'Ａ', 'S': 'Ｓ', 'D': 'Ｄ', 'F': 'Ｆ', 'G': 'Ｇ',
    'a': 'ａ', 's': 'ｓ', 'd': 'ｄ', 'f': 'ｆ', 'g': 'ｇ',
    'H': 'Ｈ', 'J': 'Ｊ', 'K': 'Ｋ', 'L': 'Ｌ', 'Z': 'Ｚ',
    'h': 'ｈ', 'j': 'ｊ', 'k': 'ｋ', 'l': 'ｌ', 'z': 'ｚ',
    'X': 'Ｘ', 'C': 'Ｃ', 'V': 'Ｖ', 'B': 'Ｂ', 'N': 'Ｎ',
    'x': 'ｘ', 'c': 'ｃ', 'v': 'ｖ', 'b': 'ｂ', 'n': 'ｎ',
    'M': 'Ｍ', 'm': 'ｍ', '=': '＝', '[': '［', ']': '］',
    ';': '；', '\'': '＇', ',': '，', '.': '。', '/': '／',
    '`': '｀', '\\': '＼', ' ': '　', '-': '－', '<': '＜', '>': '＞',
    '!': '！', '@': '＠', '#': '＃', '$': '＄', '%': '％',
    '^': '＾', '&': '＆', '*': '＊', '(': '（', ')': '）',
    '_': '＿', '+': '＋', '{': '｛', '}': '｝', '|': '｜',
    '"': '＂', ':': '：', '?': '？', '~': '～', '…': '…'
};

function switchMode() {
    // 切換鍵盤模式
    phoneticBuffer = [];
    candidateList = [];
    nextPath = [];
    $('.phonetic-container').hide();
    $('#backtick').text('`');$('#equal').text('=');$('#bracketLeft').text('[');$('#bracketRight').text(']');$('#backslash').text('\\');$('#quote').text('\'');
    if (symbolMode) {
        $('#key1').text('1');$('#keyQ').text('!');$('#keyA').text('~');$('#keyZ').text('「');
        $('#key2').text('2');$('#keyW').text('@');$('#keyS').text('_');$('#keyX').text('」');
        $('#key3').text('3');$('#keyE').text('#');$('#keyD').text('+');$('#keyC').text('…');
        $('#key4').text('4');$('#keyR').text('$');$('#keyF').text(':');$('#keyV').text('，');
        $('#key5').text('5');$('#keyT').text('%');$('#keyG').text('?');$('#keyB').text('|');
        $('#key6').text('6');$('#keyY').text('^');$('#keyH').text('<');$('#keyN').text('{');
        $('#key7').text('7');$('#keyU').text('&');$('#keyJ').text(">");$('#keyM').text('}');
        $('#key8').text('8');$('#keyI').text('*');$('#keyK').text('+');$('#comma').text(',');
        $('#key9').text('9');$('#keyO').text('(');$('#keyL').text('"');$('#period').text('.');
        $('#key0').text('0');$('#keyP').text(')');$('#semicolon').text(';');$('#slash').text('/');
        $('#minus').text('-');
    }else if (keyMode === '中' && !shiftPressed) {
        $('#key1').text('ㄅ');$('#keyQ').text('ㄆ');$('#keyA').text('ㄇ');$('#keyZ').text('ㄈ');
        $('#key2').text('ㄉ');$('#keyW').text('ㄊ');$('#keyS').text('ㄋ');$('#keyX').text('ㄌ');
        $('#key3').text('Ｖ');$('#keyE').text('ㄍ');$('#keyD').text('ㄎ');$('#keyC').text('ㄏ');
        $('#key4').text('＼');$('#keyR').text('ㄐ');$('#keyF').text('ㄑ');$('#keyV').text('ㄒ');
        $('#key5').text('ㄓ');$('#keyT').text('ㄔ');$('#keyG').text('ㄕ');$('#keyB').text('ㄖ');
        $('#key6').text('／');$('#keyY').text('ㄗ');$('#keyH').text('ㄘ');$('#keyN').text('ㄙ');
        $('#key7').text('．');$('#keyU').text('一');$('#keyJ').text('ㄨ');$('#keyM').text('ㄩ');
        $('#key8').text('ㄚ');$('#keyI').text('ㄛ');$('#keyK').text('ㄜ');$('#comma').text('ㄝ');
        $('#key9').text('ㄞ');$('#keyO').text('ㄟ');$('#keyL').text('ㄠ');$('#period').text('ㄡ');
        $('#key0').text('ㄢ');$('#keyP').text('ㄣ');$('#semicolon').text('ㄤ');$('#slash').text('ㄥ');
        $('#minus').text('ㄦ');
        $('.phonetic-container').show();
    }else if (keyMode === 'Eng' || shiftPressed) {
        $('#key1').text('1');$('#keyQ').text('Q');$('#keyA').text('A');$('#keyZ').text('Z');
        $('#key2').text('2');$('#keyW').text('W');$('#keyS').text('S');$('#keyX').text('X');
        $('#key3').text('3');$('#keyE').text('E');$('#keyD').text('D');$('#keyC').text('C');
        $('#key4').text('4');$('#keyR').text('R');$('#keyF').text('F');$('#keyV').text('V');
        $('#key5').text('5');$('#keyT').text('T');$('#keyG').text('G');$('#keyB').text('B');
        $('#key6').text('6');$('#keyY').text('Y');$('#keyH').text('H');$('#keyN').text('N');
        $('#key7').text('7');$('#keyU').text('U');$('#keyJ').text('J');$('#keyM').text('M');
        $('#key8').text('8');$('#keyI').text('I');$('#keyK').text('K');$('#comma').text(',');
        $('#key9').text('9');$('#keyO').text('O');$('#keyL').text('L');$('#period').text('.');
        $('#key0').text('0');$('#keyP').text('P');$('#semicolon').text(';');$('#slash').text('/');
        $('#minus').text('-');
        if (!capLock && !shiftPressed) {
            // 如果 Caps Lock 開啟，將所有字母轉為大寫
            document.querySelectorAll('.key').forEach(key => {
                if (key.textContent.length === 1 && /[a-z]/i.test(key.textContent)) {
                    key.textContent = key.textContent.toLocaleLowerCase();
                }
            });
        }
        if (shiftPressed) {
            // 如果 Shift 鍵被按下，模擬符號模式
            $('#backtick').text('~');$('#key1').text('!');$('#key2').text('@');$('#key3').text('#');
            $('#key4').text('$');$('#key5').text('%');$('#key6').text('^');$('#key7').text('&');
            $('#key8').text('*');$('#key9').text('(');$('#key0').text(')');$('#equal').text('+');$('#minus').text('_');
            $('#bracketLeft').text('{');$('#bracketRight').text('}');$('#backslash').text('|');
            $('#quote').text('"');$('#semicolon').text(':');$('#comma').text('<');$('#period').text('>');
            $('#slash').text('?');
        }
    }
    if (!halfWidth) {
        // 切換到全形字符
        document.querySelectorAll('.key').forEach(key => {
            const keyText = key.textContent;
            if (fullWidthChars[keyText]) {
                key.textContent = fullWidthChars[keyText];
            }
        });
    }
    updatePhoneticDisplay();
}

function getNextPossiblePath() {
    if (!InputMethodBridge || phoneticBuffer.length == 0) return;
    const nextPathJson = InputMethodBridge.getNextPossiblePath();
    console.log(`下一個可能的路徑：${nextPathJson}`);
    nextPath = JSON.parse(nextPathJson);
}

function getCandidateList() {
    if (!InputMethodBridge || phoneticBuffer.length == 0) return
    const phoneticString = phoneticBuffer.map(p => p.p).join('');
    candidateList = JSON.parse(InputMethodBridge.getCandidatesFromInput(phoneticString));
    updateCandidates(candidateList)
}

function updatePhoneticDisplay() {
    const phoneticDisplay = document.getElementById('phoneticDisplay');
    phoneticDisplay.innerHTML = '';
    for (let i = 0; i < maxPhoneticLength; i++) {
        const phoneticChar = document.createElement('div');
        phoneticChar.className = 'phonetic-char';
        if (i < phoneticBuffer.length) {
            phoneticChar.textContent = phoneticBuffer[i].w;
            phoneticChar.classList.remove('empty');
        } else {
            phoneticChar.textContent = '　';
            phoneticChar.classList.add('empty');
        }
        phoneticDisplay.appendChild(phoneticChar);
    }
}

function clearPhonetic() {
    phoneticBuffer = [];
    candidateList = [];
    nextPath = ['*'];
    updatePhoneticDisplay();
}

function isNextPathAvailable(keyValue) {
    if (nextPath.length === 0) {
        return false;
    }else{
        if (nextPath[0]=='*'){
            return true;
        }
    }
    return nextPath.includes(keyValue);
}

function addPhonetic(char, keyValue) {
    if (phoneticBuffer.length < maxPhoneticLength) {
        if (candidateList.length > 0 && phoneticBuffer.length !== 0) {
            if (!isNextPathAvailable(keyValue)) {
                $('#candidate1').click();
            }
        }
        phoneticBuffer.push({w: char, p: keyValue});
        updatePhoneticDisplay();
        getCandidateList();
        getNextPossiblePath();
        return true;
    }
    return false;
}

function backspacePhonetic() {
    if (phoneticBuffer.length > 0) {
        phoneticBuffer.pop();
        updatePhoneticDisplay();
        getCandidateList();
        getNextPossiblePath();
        return true;
    }
    if (phoneticBuffer.length === 0) {
        nextPath = ['*']; // 清除下一個可能的路徑
    }
    return false;
}


function updateCandidates(candidates) {
    const candidatesRow = document.getElementById('candidatesRow');
    candidatesRow.innerHTML = '';
    candidates.forEach((candidate, index) => {
        let word = candidate;
        let path = '';
        if (typeof candidate !== 'string') {
            word = candidate.w
            path = candidate.p;
        }
        const candidateElement = document.createElement('div');
        candidateElement.setAttribute('data-value', word);
        candidateElement.setAttribute('data-path', path);
        candidateElement.className = 'candidate';
        candidateElement.id = `candidate${index + 1}`;
        candidateElement.innerHTML = `
            <span class="number">${index + 1}</span>
            ${word}
        `;
        candidatesRow.appendChild(candidateElement);
    });
    document.querySelectorAll('.candidate').forEach(candidate => {
        candidate.addEventListener('click', function() {
            const selectedValue = this.getAttribute('data-value');
            console.log(`選擇候選字：${selectedValue}`);
            if (phoneticBuffer.length > 0) {
                clearPhonetic();
            }
            insertTextAtCursor(selectedValue);
            // 清除候選字顯示
            this.parentNode.innerHTML = '';
        });
    });
}

function copyToClipboard(text) {
    if (InputMethodBridge){
        InputMethodBridge.copyToClipboard(text);
    }
}

function insertTextAtCursor(text) {
    const textArea = document.getElementById('textArea');
    const start = textArea.selectionStart;
    const end = textArea.selectionEnd;
    const currentValue = textArea.value;
    textArea.value = currentValue.slice(0, start) + text + currentValue.slice(end);
    textArea.selectionStart = textArea.selectionEnd = start + text.length;
}

document.querySelectorAll('.key').forEach(key => {
    key.addEventListener('click', function() {
        const keyId = this.id;
        const keyText = this.textContent;
        const isSpecial = this.getAttribute('data-special') == '1';
        const keyValue = this.getAttribute('data-value');
        console.log(`按下鍵：${keyId} (${keyText})`);
        if (keyId === 'backspace') {
            if (!backspacePhonetic()) {
                // 如果注音暫存區沒有內容，對文字區進行刪除
                const textArea = document.getElementById('textArea');
                const currentValue = textArea.value;
                const selectedStart = textArea.selectionStart;
                const selectedEnd = textArea.selectionEnd;
                if (selectedStart === selectedEnd && selectedStart > 0) {
                    // 如果沒有選取文字，刪除光標前一個字元
                    textArea.value = currentValue.slice(0, selectedStart - 1) + currentValue.slice(selectedEnd);
                    textArea.selectionStart = textArea.selectionEnd = selectedStart - 1;
                } else {
                    // 如果有選取文字，刪除選取的文字
                    textArea.value = currentValue.slice(0, selectedStart) + currentValue.slice(selectedEnd);
                    textArea.selectionStart = textArea.selectionEnd = selectedStart;
                }
            }
        } else if (keyId === 'enter') {
            console.log('ENTER');
        } else if (keyId === 'ctrlLeft' || keyId === 'ctrlRight') {
            console.log('複製文字');
            copyToClipboard(document.getElementById('textArea').value);
        } else if (keyText.length === 1 && !isSpecial) {
            if (keyMode == '中'){
                addPhonetic(keyText, keyValue);
            }else{
                insertTextAtCursor(keyText);
            }
        }
    });
});

// 初始化注音顯示
updatePhoneticDisplay();

// 使用範例
updateCandidates(['中', '文', '輸入法', 'by', 'Weil Jimmer']);

// special key handling

// 模式事件
$('#altLeft').on('click', () => {
    keyMode = keyMode === '中' ? 'Eng' : '中';
    $('#altLeft').text(keyMode);
    switchMode();
});
$('#altRight').on('click', () => {
    halfWidth = !halfWidth;
    $('#altRight').text(halfWidth ? '半' : '全');
    switchMode();
});
$('#capslock').on('click', ()=>{
    capLock = !capLock;
    $('#capslock').text((capLock?'🔴 ':'') + '大寫');
    switchMode();
});
$('#symbol').on('click', ()=>{
    symbolMode = !symbolMode;
    $('#symbol').text((symbolMode?'🔴 ':'') + '?123');
    switchMode();
});
$('.shift').on('click', ()=>{
    shiftPressed = !shiftPressed;
    $('.shift').text((shiftPressed?'🔴 ':'') + 'Shift');
    switchMode();
});
switchMode();
document.getElementById('clearPhonetic').addEventListener('click', ()=>{document.getElementById('textArea').value = '';});