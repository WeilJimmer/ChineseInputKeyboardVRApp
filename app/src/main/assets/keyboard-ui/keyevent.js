// keyevent.js
// 處理鍵盤事件和注音輸入

const Mode = Object.freeze({
  ENG: 'Eng',
  ZHUYIN: '注'
});

// ========== Mode Region ==========
var keyMode = Mode.ENG // 預設為英文模式
var capLock = false; // Caps Lock 狀態
var symbolMode = false; // 符號模式
var halfWidth = true; // 半形狀態
var shiftPressed = false; // Shift 鍵狀態

// ========== Input Region ==========
var phoneticBuffer = [];
var candidateList = []; // 候選字列表
var nextPath = ['*']; // 下一個可能的路徑

// ========== UI Region ==========
var currentPage = 0; // 當前頁面
var hasNextPage = false; // 是否有下一頁
const maxPhoneticLength = 4;
const zhuyinChars = "abcdefghijklmnopqrstuvwxyz0123456789-;,./ " // 注音符號字符集 ㄅ~ㄦ 含 五個調號
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

/*
    事件綑綁器
*/
function bindFastClick(selector, handler) {
    const $el = $(selector);
    let touchTimer = null;

    $el.on('touchend', function (e) {
        e.preventDefault();
        if (touchTimer) clearTimeout(touchTimer);
        touchTimer = setTimeout(() => {
            handler(e);
            touchTimer = null;
        }, 50); // 防止雙重觸發
    });

    $el.on('click', function (e) {
        if (!touchTimer) handler(e);
    });
}

/*
    多重事件綑綁器
    用於綁定多個元素到事件上
*/
function bindFastClickMultiple(selector, handler) {
    document.querySelectorAll(selector).forEach(el => {
        let touched = false;

        el.addEventListener('touchend', function (e) {
            touched = true;
            handler.call(this, e);
        });

        el.addEventListener('click', function (e) {
            if (!touched) handler.call(this, e);
            touched = false;
        });
    });
}

/*
    切換鍵盤模式
*/
function switchMode() {
    clearPhonetic();
    $('.key').removeClass('phonetic');
    $('.phonetic-container').hide();
    $('#backtick').text('`');$('#equal').text('=');$('#bracketLeft').text('[');$('#bracketRight').text(']');$('#backslash').text('\\');$('#quote').text('\'');$('#keySpace').text(' ');
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
    }else if (keyMode === Mode.ZHUYIN && !shiftPressed) {
        $('.key').addClass('phonetic');
        $('#key1').text('ㄅ');$('#keyQ').text('ㄆ');$('#keyA').text('ㄇ');$('#keyZ').text('ㄈ');
        $('#key2').text('ㄉ');$('#keyW').text('ㄊ');$('#keyS').text('ㄋ');$('#keyX').text('ㄌ');
        $('#key3').text('˅');$('#keyE').text('ㄍ');$('#keyD').text('ㄎ');$('#keyC').text('ㄏ');
        $('#key4').text('⸌');$('#keyR').text('ㄐ');$('#keyF').text('ㄑ');$('#keyV').text('ㄒ');
        $('#key5').text('ㄓ');$('#keyT').text('ㄔ');$('#keyG').text('ㄕ');$('#keyB').text('ㄖ');
        $('#key6').text('⁄');$('#keyY').text('ㄗ');$('#keyH').text('ㄘ');$('#keyN').text('ㄙ');
        $('#key7').text('．');$('#keyU').text('一');$('#keyJ').text('ㄨ');$('#keyM').text('ㄩ');
        $('#key8').text('ㄚ');$('#keyI').text('ㄛ');$('#keyK').text('ㄜ');$('#comma').text('ㄝ');
        $('#key9').text('ㄞ');$('#keyO').text('ㄟ');$('#keyL').text('ㄠ');$('#period').text('ㄡ');
        $('#key0').text('ㄢ');$('#keyP').text('ㄣ');$('#semicolon').text('ㄤ');$('#slash').text('ㄥ');
        $('#minus').text('ㄦ');
        $('.phonetic-container').show();
    }else if (keyMode === Mode.ENG || shiftPressed) {
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
        if ((!capLock && !shiftPressed) || (capLock && shiftPressed)) {
            // 如果 Caps Lock 開啟，將所有字母轉為小寫
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

function setInput() {
    if (typeof InputMethodBridge === 'undefined' || phoneticBuffer.length == 0) return;
    const phoneticString = phoneticBuffer.map(p => p.p).join('');
    if (phoneticString!== '') {
        processCandidateList(InputMethodBridge.getCandidatesFromInput(phoneticString));
    } else {
        console.error('無法獲取候選字列表');
    }
}

function processCandidateList(candidateResultJson) {
    let candidateResult = JSON.parse(candidateResultJson);
    candidateList = candidateResult.words || [];
    nextPath = candidateResult.chars || ['*'];
    hasNextPage = candidateResult.hasNextPage || false;
    updateCandidates(candidateList)
    console.log(`下一個可能的路徑：${nextPath}`);
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

/*
    清除注音暫存區和候選字列表
*/
function clearPhonetic() {
    phoneticBuffer = [];
    candidateList = [];
    nextPath = ['*'];
    currentPage = 0;
    hasNextPage = false;
    updatePhoneticDisplay();
}

function isNextPathAvailable(keyValue='') {
    if (nextPath.length === 0) {
        return false;
    }else{
        if (nextPath[0]=='*'){
            return true;
        }
    }
    if (keyValue!=''){
        return nextPath.includes(keyValue);
    }
    return nextPath.length!==0; // 如果有下一個可能的路徑，則返回 true
}

function isPhoneticEmpty() {
    // 檢查注音暫存區是否為空
    return phoneticBuffer.length === 0;
}

function switchCandidateSelection() {
    const candidates = $('.candidate-word');
    if (candidates.length === 0) return; // 如果沒有候選字，則不進行任何操作
    const selectedCandidate = $('.candidate-selected');
    let nextIndex = (candidates.index(selectedCandidate) + 1) % candidates.length; // 循環選擇候選字
    selectedCandidate.removeClass('candidate-selected'); // 移除當前選中的候選字樣式
    const nextCandidate = $(candidates[nextIndex]);
    nextCandidate.addClass('candidate-selected'); // 添加選中樣式
    nextCandidate[0].scrollIntoView({ behavior: 'smooth', block: 'nearest' }); // 滾動到選中的候選字
    console.log(`切換到候選字：${nextCandidate.text()}`);
}

function insertSelectedCandidate() {
    if ($('.candidate-selected').length === 0) {
        console.warn('沒有選中的候選字，無法插入。');
        return false;
    }
    $('.candidate-selected').click();
    return true; // 返回 true 表示成功插入候選字
}

function addPhonetic(char, keyValue) {
    if (phoneticBuffer.length < maxPhoneticLength) {
        if (candidateList.length > 0 && phoneticBuffer.length !== 0) {
            if (!isNextPathAvailable(keyValue)) {
                // 當前輸入的注音符號不在下一個可能的路徑中，則插入候選字，清除注音暫存區
                insertSelectedCandidate();
            }
        }
        phoneticBuffer.push({w: char, p: keyValue});
        updatePhoneticDisplay();
        setInput();
        return true;
    }else if (phoneticBuffer.length === maxPhoneticLength) {
        // 當注音暫存區已滿，則插入候選字，清除注音暫存區
        insertSelectedCandidate();
        phoneticBuffer = []; // 清除注音暫存區
        updatePhoneticDisplay();
    }
    return false;
}

function backspacePhonetic() {
    if (phoneticBuffer.length > 0) {
        phoneticBuffer.pop();
        updatePhoneticDisplay();
        setInput();
        return true;
    }
    // 如果注音暫存區已經沒有內容，則不進行任何操作
    nextPath = ['*']; // 清除下一個可能的路徑
    return false;
}

function candidateSelected(word, path='') {
    insertTextAtCursor(word);
    if (typeof InputMethodBridge !== 'undefined' && path!='') {
        InputMethodBridge.setPromoteCandidate(JSON.stringify({"w": word, "p": path}));
        console.log(`選擇候選字：${word}，路徑：${path} 已提交給輸入法管理器優化`);
    } else {
        console.error('InputMethodBridge is not defined. Cannot handle candidate selection.');
    }
}

function updateCandidates(candidates) {
    let candidateRow = document.getElementById('candidateRow');
    const newCandidateRow = document.createElement('div');
    newCandidateRow.id = 'candidateRow';
    newCandidateRow.className = candidateRow.className;
    candidateRow.parentNode.replaceChild(newCandidateRow, candidateRow);
    candidateRow = newCandidateRow; // 更新引用
    // 清除現有候選字
    candidateRow.innerHTML = '';
    if (hasNextPage){
        candidates.push({
            w : '▶️',
            p : '|next|'
        });
    }
    if (currentPage>0){
        candidates.unshift({
            w : '◀️',
            p : '|prev|'
        });
    }
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
        if (path=='|next|'){
            candidateElement.id = 'next_candidate';
            candidateElement.classList.add('next-candidate');
            candidateElement.title = '下一頁';
        }else if (path=='|prev|'){
            candidateElement.id = 'prev_candidate';
            candidateElement.classList.add('prev-candidate');
            candidateElement.title = '上一頁';
        }else{
            candidateElement.classList.add('candidate-word');
            candidateElement.id = `candidateWord${index + 1}`;
            if (index==0){
                candidateElement.classList.add('candidate-selected'); // 選中第一個候選字
            }
        }
        candidateElement.innerHTML = `
            <span class="number">${index + 1}</span>
            ${word}
        `;
        candidateRow.appendChild(candidateElement);
    });
    bindFastClickMultiple('.candidate-word', function () {
        const selectedValue = this.getAttribute('data-value');
        const path = this.getAttribute('data-path');
        console.log(`選擇候選字：${selectedValue}`);
        if (path === '|next|' || path === '|prev|') return;
        if (phoneticBuffer.length > 0) {
            clearPhonetic();
        }
        candidateSelected(selectedValue, path);
        // 清除候選字顯示
        candidateRow.innerHTML = '';
    });
    // 處理下一頁按鈕
    bindFastClick('.next-candidate', function(e) {
        e.stopPropagation();
        if (hasNextPage) {
            currentPage++;
            console.log(`切換到下一頁：${currentPage}`);
            processCandidateList(InputMethodBridge.getNextCandidates());
        } else {
            console.log('沒有下一頁');
        }
    });
    // 處理上一頁按鈕
    bindFastClick('.prev-candidate', function(e) {
        e.stopPropagation();
        if (currentPage > 0) {
            currentPage--;
            console.log(`切換到上一頁：${currentPage}`);
            processCandidateList(InputMethodBridge.getPrevCandidates());
        } else {
            console.log('已經是第一頁');
        }
    });
}

function copyToClipboard(text) {
    if (typeof InputMethodBridge === 'undefined'){
        console.error('InputMethodBridge is not defined. Cannot copy to clipboard.');
        alert('無法複製到剪貼簿，請檢查輸入法橋接是否正確。');
        return;
    }else{
        InputMethodBridge.copyToClipboard(text.replace(/\t/g, '\t'));
    }
}

function insertTextAtCursor(text) {
    if (text==null) return;
    const textArea = document.getElementById('textArea');
    const start = textArea.selectionStart;
    const end = textArea.selectionEnd;
    const currentValue = textArea.value;
    textArea.value = currentValue.slice(0, start) + text + currentValue.slice(end);
    textArea.selectionStart = textArea.selectionEnd = start + text.length;
}

// 綁定鍵盤按鍵事件
bindFastClickMultiple('.key', function() {
    const key = $(this);
    const keyId = key.attr('id');
    const keyText = key.text();
    const isSpecial = key.attr('data-special') == '1';
    const keyValue = key.attr('data-value');
    console.log(`按下鍵：${keyId} (${keyText}) value: ${keyValue}, isSpecial: ${isSpecial}`);
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
    } else if (keyId === 'tab') {
        console.log('TAB');
        switchCandidateSelection();
    } else if (keyId === 'enter') {
        console.log('ENTER');
        if (!insertSelectedCandidate()){
            insertTextAtCursor('\n');
        }
    } else if (keyId === 'ctrlLeft' || keyId === 'ctrlRight') {
        console.log('複製文字');
        copyToClipboard(document.getElementById('textArea').value);
    } else if (keyValue!=null && keyValue.length === 1 && !isSpecial) {
        if (keyMode == Mode.ZHUYIN && !shiftPressed && !symbolMode) {
            // 如果是注音模式，且沒有按下 Shift 鍵或符號模式
            if (zhuyinChars.includes(keyValue)) {
                if (keyValue === ' ' && isPhoneticEmpty()) {
                    // 如果按下空格鍵且注音暫存區為空，則插入空格
                    insertTextAtCursor(keyText); // 插入空格
                } else if (keyValue === ' ' && (!isNextPathAvailable())) {
                    // 如果按下空格鍵且注音暫存區已滿，則插入候選字
                    insertSelectedCandidate();
                    insertTextAtCursor(keyText); // 插入空格
                } else {
                    addPhonetic(keyText, keyValue);
                }
            } else {
                insertTextAtCursor(keyText);
            }
        }else{
            insertTextAtCursor(keyText);
        }
    }
});

// 初始化注音顯示
updatePhoneticDisplay();

// 使用範例
updateCandidates(['中', '文', '輸入法', 'by', 'Weil Jimmer']);

// 模式事件
bindFastClick('#altLeft', () => {
    keyMode = keyMode === Mode.ZHUYIN ? Mode.ENG : Mode.ZHUYIN;
    $('#altLeft').text(keyMode);
    switchMode();
});

bindFastClick('#altRight', () => {
    halfWidth = !halfWidth;
    $('#altRight').text(halfWidth ? '半' : '全');
    switchMode();
});

bindFastClick('#capslock', () => {
    capLock = !capLock;
    $('#capslock').text((capLock ? '🔴 ' : '') + '大寫');
    switchMode();
});

bindFastClick('#symbol', () => {
    symbolMode = !symbolMode;
    $('#symbol').text((symbolMode ? '🔴 ' : '') + '?123');
    switchMode();
});

bindFastClick('.shift', () => {
    shiftPressed = !shiftPressed;
    $('.shift').text((shiftPressed ? '🔴 ' : '') + 'Shift');
    switchMode();
});

bindFastClick('#clearPhonetic', () => {
    clearPhonetic();
    updateCandidates([]);
});

bindFastClick('#clearTextBtn', () => {
    $('#textArea').val('');
    clearPhonetic();
    updateCandidates([]);
    console.log('清除文字區和注音暫存區');
});

bindFastClick('#pasteTextBtn', () => {
    insertTextAtCursor(InputMethodBridge.getClipboardText());
});

bindFastClick('#copyTextBtn', () => {
    copyToClipboard(document.getElementById('textArea').value);
});

// 初始化按鈕事件
switchMode();