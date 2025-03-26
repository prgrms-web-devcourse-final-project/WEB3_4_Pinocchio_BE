import ReactQuill, { Quill } from "react-quill";
import 'react-quill/dist/quill.snow.css';
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import axios from "axios";
import ImageResize from 'quill-image-resize';
import {buildQuery} from "../../utils/utils";
import {encode} from "html-entities";
//import styled from "styled-components";
Quill.register('modules/ImageResize', ImageResize);

const ImageFormat = Quill.import('formats/image');
ImageFormat.className = 'ql-image';
ImageFormat.allowedAttributes = ['src', 'alt', 'style', 'width'];
Quill.register(ImageFormat, true);

const Inline = Quill.import("blots/inline");
class MentionBlot extends Inline {
    static create(value) {
        const node = super.create();
        node.setAttribute("data-id", value.id);
        node.setAttribute("class", "mention");
        node.innerText = value.userFullName;
        return node;
    }

    static value(node) {
        return {
            id: node.getAttribute("data-id"),
            userFullName: node.innerText,
        };
    }
}

MentionBlot.blotName = "mention";
MentionBlot.tagName = "u";
Quill.register(MentionBlot, true);

const whitelistAttributes = ['data-uploadid']; // 허용할 속성
const Clipboard = Quill.import('modules/clipboard');
Clipboard.MATCHERS = Clipboard.MATCHERS || [];
Clipboard.MATCHERS.push([
    Node.ELEMENT_NODE,
    (node, delta) => {
        whitelistAttributes.forEach((attr) => {
            if (node.hasAttribute(attr)) {
                delta.ops.forEach((op) => {
                    if (!op.attributes) {
                        op.attributes = {};
                    }
                    op.attributes[attr] = node.getAttribute(attr);
                });
            }
        });
        return delta;
    },
]);
Quill.register('modules/clipboard', Clipboard);

const Indent = Quill.import('formats/indent');
Quill.register(Indent, true);

/*const QuillContainer = styled.div`
    .ql-indent-1 {
        padding-left: 30px;
    }

    .ql-indent-2 {
        padding-left: 60px;
    }

    .ql-indent-3 {
        padding-left: 90px;
    }

    .ql-indent-4 {
        padding-left: 120px;
    }

    .ql-indent-5 {
        padding-left: 150px;
    }

    .ql-indent-6 {
        padding-left: 180px;
    }
`;*/

const UpdateEditor = (
    {
        content,                            // 수정 할 내용
        data,                               // 저장 버튼 클릭 시 넘길 데이터
        save,                               // 저장 버튼 클릭 시 작동 메서드
        clickCancelButton,                  // 취소 버튼 클릭 시 작동 메서드
        setEditorContent,                   // 에디터의 내용 세팅 메서드
        showFooter = true,         // footer 출력 여부(글자수, 저장, 취소 버튼)
        showSaveButton = true,     // 저장 버튼 출력 여부
        showCancelButton = true,   // 취소 버튼 출력 여부
        saveButtonText = "저장",      // 저장 버튼명
        maxChars = 3000,            // 최대 허용 글자 수
        width = '100%',               // 가로 길이
        height = '200px',              // 세로 길이
        maxHeight = '800px',            // 최대 세로 길이
        minHeight = '100px',             // 최소 세로 길이
        userList,                              // @입력 시 멘션박스에 보여줄 기본 유저목록
        mentionBoxWidth,                        // 멘션박스 길이
        toolbar =  [
            [{ 'header': [1, 2, 3, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ 'list': 'ordered' }, { 'list': 'bullet' },{ indent: "-1" },
                { indent: "+1" },],
            [{ 'color': [] }],
            ['link', 'image' ],
        ]                                      // 기본 툴바
    }) => {

    // 에디터 작성 데이터
    const [editorValue, setEditorValue] = useState('');
    // 현재 글자 수
    const [textLength, setTextLength] = useState(0);
    // 허용 가능한 첨부파일 확장자
    const acceptedExtensions = ['doc', 'docx', 'xls', 'xlsx', 'jpeg', 'jpg', 'png', 'zip', 'csv', 'pdf', 'txt'];
    // 툴바 아이콘
    const formats = ['header', 'bold', 'italic', 'underline', 'strike', 'list', 'bullet', 'indent', 'color', 'link', 'image', 'data-uploadid'];
    // 툴바 높이를 저장할 상태
    const [toolbarHeight, setToolbarHeight] = useState(0);
    // 툴바 제외 에디터 본문의 높이
    const [editorBodyHeight, setEditorBodyHeight] = useState('');
    // 툴바 제외 에디터 본문 최대 높이
    const [editorMaxHeight, setEditorMaxHeight] = useState(0);
    // 툴바 제외 에디터 본문 최소 높이
    const [editorMinHeight, setEditorMinHeight] = useState(0);

    const quillRef = useRef(null);

    // 툴바 제외한 에디터 높이 구하기
    useEffect(() => {
        if (quillRef.current) {
            const toolbarElement = quillRef.current.getEditor().container.previousSibling;
            const toolbarHeight = toolbarElement.clientHeight;  // 툴바의 실제 높이 가져오기
            setToolbarHeight(toolbarHeight);
            setEditorBodyHeight(`calc(100% - ${toolbarHeight+3}px)`);
            setEditorMaxHeight(`calc(${maxHeight} - ${toolbarHeight+3}px)`)
            setEditorMinHeight(`calc(${minHeight} - ${toolbarHeight+3}px)`)

        }
    }, [height]);

    // prop으로 받은 컨텐츠를 에디터에 세팅
    useEffect(() => {
        if (quillRef.current && content) {
            const quillEditor = quillRef.current.getEditor();
            quillEditor.setContents([]);

            // 1. 콘텐츠에서 이미지 및 a 태그의 속성 정보 추출
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = content;

            // 이미지 태그 속성 추출
            const imageAttributes = Array.from(tempDiv.querySelectorAll('img')).map(img => ({
                src: img.getAttribute('src'),
                uploadId: img.getAttribute('data-uploadid'),
                width: img.getAttribute('width'),
            }));

            // a 태그 속성 추출
            const anchorAttributes = Array.from(tempDiv.querySelectorAll('a')).map(anchor => ({
                href: anchor.getAttribute('href'),
                uploadId: anchor.getAttribute('data-uploadid'),
            }));

            // 2. 콘텐츠를 Quill 에디터에 삽입
            quillEditor.clipboard.dangerouslyPasteHTML(0, content);

            // 3. 삽입된 이미지 태그에 다시 속성 복원
            const insertedImages = quillEditor.root.querySelectorAll('img');
            insertedImages.forEach((img, index) => {
                const { src, uploadId, width } = imageAttributes[index] || {};
                if (src) img.setAttribute('src', src);
                if (uploadId) img.setAttribute('data-uploadid', uploadId);
                if (width) img.setAttribute('width', width);
            });

            // 4. 삽입된 a 태그에 다시 속성 복원
            const insertedAnchors = quillEditor.root.querySelectorAll('a');
            insertedAnchors.forEach((anchor, index) => {
                const { uploadId } = anchorAttributes[index] || {};
                if (uploadId) anchor.setAttribute('data-uploadid', uploadId);
            });
        }
    }, [content]);

    // 파일 업로드 핸들러
    const fileHandler = useCallback(() => {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.click();

        input.onchange = async () => {
            const file = input.files[0];
            if (file) {
                await handleFileInsert(file);
            }
        };
    }, []);

    // 파일 업로드 처리 메서드
    const handleFileInsert = async (file) => {
        const fileExtension = file.name.split('.').pop().toLowerCase();

        if (!acceptedExtensions.includes(fileExtension)) {
            /*Alert({
                title: '잘못된 파일 형식입니다.',
                html: '워드, 엑셀, 이미지, ZIP, CSV, PDF 파일만 업로드할 수 있습니다.',
                icon: 'warning',
                confirmButtonText: '확인',
            });*/
            return;
        }

        const reader = new FileReader();
        reader.onload = async () => {
            if (quillRef.current) {
                const quillEditor = quillRef.current.getEditor();
                const range = quillEditor.getSelection();
                // 파일 식별값
                const uniqueId = `file-${Date.now()}`;

                let fileTag;

                // 업로드한 파일에 식별값 부여
                if (file.type.startsWith('image/')) {
                    quillEditor.insertEmbed(range.index, 'image', uniqueId);
                    quillEditor.setSelection(range.index + 1);
                } else {
                    const fileName = file.name;
                    fileTag = `<a href="${uniqueId}">${fileName}</a>`;
                }

                // 업로드한 파일 에디터에 출력
                if (range) {
                    quillEditor.clipboard.dangerouslyPasteHTML(range.index, fileTag);
                } else {
                    quillEditor.clipboard.dangerouslyPasteHTML(quillEditor.getLength(), fileTag);
                }

                const imgElement = quillEditor.root.querySelector(`img[src="${uniqueId}"]`);
                const fileElement = quillEditor.root.querySelector(`[href="${uniqueId}"]`);

                // 업로드한 파일 서버에 임시 저장
                const uploadId = await handleFileUpload(file);

                // 업로드한 파일에 uploadId 부여
                if (imgElement && uploadId) {
                    imgElement.setAttribute('src', reader.result);  // 실제 업로드된 이미지 URL로 교체
                    imgElement.setAttribute('data-uploadtempid', uploadId);
                    imgElement.setAttribute('width', 300);
                } else if (fileElement && uploadId) {
                    fileElement.setAttribute('data-uploadtempid', uploadId);
                    fileElement.setAttribute('href', 'javascript:void(0);');
                    fileElement.removeAttribute('target');
                }
            }
        };
        reader.readAsDataURL(file);
    };

    // 임시파일 저장
    const handleFileUpload = async (file) => {
        const formData = new FormData();
        formData.append("files", file);
        const uploadUrl = '/api/upload/temp';
        try {
            const response = await axios.post(uploadUrl, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            // 반환받은 uploadTempId 리턴
            const uploadTempId = response.data[0].uploadTempId;
            return uploadTempId
        } catch (err) {
            console.log(err);
            /*Alert({
                title: '파일첨부 중 오류가 발생했습니다.',
                icon: 'warning',
                confirmButtonText: '확인',
            });*/
        }
    };

    // 복사/붙여넣기 처리
    const handlePaste = useCallback((event) => {
        const clipboardData = event.clipboardData || window.clipboardData;
        const items = clipboardData.items;

        for (let item of items) {
            if (item.kind === 'file' && item.type.startsWith('image/')) {
                event.preventDefault(); // 기본 동작 막기
                const file = item.getAsFile();
                handleFileInsert(file); // 파일 업로드 처리
            }
        }
    }, []);

    // handlePaste 메서드를 이벤트 핸들러 등록
    useEffect(() => {
        if (quillRef.current) {
            const quillEditor = quillRef.current.getEditor();
            quillEditor.root.addEventListener('paste', handlePaste); // Paste 이벤트 리스너 추가

            return () => {
                quillEditor.root.removeEventListener('paste', handlePaste);
            };
        }
    }, [handlePaste]);

    // 링크 핸들러
    const customLinkHandler = () => {
        const quill = quillRef.current.getEditor()
        const range = quill.getSelection();  // 텍스트 선택 범위 가져오기

        // 링크 추가할 텍스트가 선택되어 있는지 확인
        if (range && range.length > 0) {
            const url = prompt('URL을 입력해 주세요.');  // 사용자에게 URL 입력 받기
            if (url) {
                quill.format('link', url);  // 선택된 텍스트에 링크 적용
            }
        } else {
            /*Confirm({
                title: '링크를 적용할 텍스트를 먼저 선택하세요.',
                icon: 'info',
                confirmButtonText: '확인',
                showCancelButton: false,
            });*/
        }
    };

    // 툴바 아이콘에 툴팁 추가
    useEffect(() => {
        if (quillRef.current) {
            const toolbar = quillRef.current.getEditor().container.previousSibling;
            const buttons = toolbar.querySelectorAll('button');
            const spans = toolbar.querySelectorAll('span');

            const toolbarOptionLabels = {
                'bold': '굵게',
                'italic': '기울임',
                'underline': '밑줄',
                'strike': '취소선',
                'link': '링크 추가',
                'image': '파일 추가',
                'list-ordered': '번호형 목록',
                'list-bullet': '불릿 목록',
                'color': '글자색',
            };

            // 각 버튼의 클래스에서 'ql-'로 시작하는 클래스를 찾아 title 속성 추가
            buttons.forEach(button => {
                const buttonClass = Array.from(button.classList).find(cls => cls.startsWith('ql-'));
                let className = buttonClass?.replace('ql-', '');

                // 리스트 버튼의 경우 ordered와 bullet 구분
                if (className === 'list') {
                    const listType = button.value === 'ordered' ? 'list-ordered' : 'list-bullet';
                    className = listType;
                }

                if (className && toolbarOptionLabels[className]) {
                    button.setAttribute('title', toolbarOptionLabels[className]);
                }
            });

            // span 요소에 title 추가 (color 같은 드롭다운 메뉴)
            spans.forEach(span => {
                const format = span.classList.contains('ql-color') ? 'color' : null;

                if (format && toolbarOptionLabels[format]) {
                    span.setAttribute('title', toolbarOptionLabels[format]);
                }
            });
        }
    }, [quillRef]);

    // 에디터 텍스트 변할 시
    const handleTextChange = (content, delta, source, editor) => {
        const text = editor.getText(); // 에디터의 순수 텍스트만 가져옴 (HTML 제외)
        // 에디터 값에 내용 세팅
        setEditorValue(content);
        // 현재 글자 수 업데이트
        setTextLength(text.trim().length);
        // setEditorContent 메서드 작동
        if (quillRef.current && setEditorContent) {
            const quillEditor = quillRef.current.getEditor();
            let editorHtml = quillEditor.root.innerHTML;

            // 공백을 &nbsp;로 변환
            editorHtml = editorHtml.replace(/ {2}/g, match => '&nbsp;'.repeat(match.length));

            setEditorContent(encode(editorHtml));
        }
    };

    // 저장 버튼 클릭
    const handleSubmit = () => {
        if (textLength > maxChars) {
            /*Confirm({
                title: '글자수 초과',
                html: `최대 ${maxChars}자까지 입력할 수 있습니다.`,
                icon: 'warning',
                confirmButtonText: '확인',
                showCancelButton: false,
            });*/
            return;
        }

        if (quillRef.current) {
            const quillEditor = quillRef.current.getEditor();
            let editorHtml = quillEditor.root.innerHTML;

            // 공백을 &nbsp;로 변환
            editorHtml = editorHtml.replace(/ {2}/g, match => '&nbsp;'.repeat(match.length));

            // 저장 함수 작동
            if (save) {
                save(encode(editorHtml), data);
            }
            // 에디터 내용 비움
            setEditorValue('');
        }
    };

    // 취소 버튼 클릭
    const handleCancel = () => {
        // 에디터 내용을 빈 문자열로 설정 (초기화)
        setEditorValue('');

        // 프랍으로 받은 메서드 호출
        if (clickCancelButton) {
            clickCancelButton();
        }
    };

    const modules = useMemo(() => ({
        toolbar: {
            container: toolbar,
            handlers: {
                'image': fileHandler,
                'link': customLinkHandler
            }
        },
        ImageResize: {
            parchment: Quill.import('parchment')
        },
        clipboard: {
            matchVisual: false,
        },
    }), [fileHandler]);


    // 멘션 기능 메서드 및 변수 들
    // 멘션 유저 박스 출력 여부
    const [mentionBoxVisible, setMentionBoxVisible] = useState(false);
    // 멘션 박스 출력 할 위치
    const [mentionBoxPosition, setMentionBoxPosition] = useState({ top: 0, left: 0 });
    // 멘션 박스 ref
    const mentionBoxRef = useRef(null); // 멘션 박스의 높이를 참조하기 위한 ref
    // 멘션 박스 출력 위치를 정하기 위한 @ 위치 저장
    const [mentionTriggerBounds, setMentionTriggerBounds] = useState(null);
    // 멘션 사용자 검색어
    const [mentionKeyword, setMentionKeyword] = useState("");
    // 멘션 유저 목록
    const [mentionUserList, setMentionUserList] = useState([]);
    // 멘션 박스 유저 목록에서 선택중인 데이터의 순서
    const [selectedIndex, setSelectedIndex] = useState(0);
    // 현재 커서 위치 저장
    const [savedCursorPosition, setSavedCursorPosition] = useState(null);

    // 멘션 박스 위치 설정
    // 에디터의 높이와 @의 위치를 이용해 멘션박스의 위치 설정
    useEffect(() => {
        if (mentionBoxVisible && mentionBoxRef.current && mentionTriggerBounds) {
            const quillEditor = quillRef.current.getEditor();
            // 에디터 커서의 위치
            const cursorBounds = quillEditor.getBounds(quillEditor.getSelection()?.index || 0);

            const editorContainer = quillEditor.root.parentNode;
            const computedHeight = getComputedStyle(editorContainer).height;
            const computedWidth = getComputedStyle(editorContainer).width;
            // 에디터 길이, 높이
            const editorWidth = parseInt(computedWidth, 10);
            const editorHeight = parseInt(computedHeight, 10);
            // 풋터 높이
            const footerHeight = showFooter ? 60 : 0;
            // @ 바로 위에 위치
            setMentionBoxPosition({
                bottom: editorHeight - cursorBounds.top + footerHeight,
                left: 20,
                width: mentionBoxWidth || editorWidth - 20
            });
        }

    }, [mentionBoxVisible, mentionTriggerBounds, mentionBoxRef]);

    // 키보드 클릭 이벤트 생성
    useEffect(() => {
        if (mentionBoxVisible) {
            const handleKeyDown = (event) => {
                if (mentionUserList.length > 0) {
                    // 위 아래 화살표 클릭 시 셀렉트인덱스 값 수정
                    if (event.key === "ArrowDown") {

                        event.preventDefault();
                        setSelectedIndex((prevIndex) =>
                            prevIndex + 1 < mentionUserList.length ? prevIndex + 1 : 0
                        ); // 마지막 데이터에서 아래로 이동 시, 첫 번째로 돌아감

                    } else if (event.key === "ArrowUp") {

                        event.preventDefault();
                        setSelectedIndex((prevIndex) =>
                            prevIndex - 1 >= 0 ? prevIndex - 1 : mentionUserList.length - 1
                        ); // 첫 번째 데이터에서 위로 이동 시, 마지막으로 돌아감

                    } else if (event.key === "Enter") {
                        // 엔터 버튼 클릭 시 handleUserClick 메서드 실행
                        event.preventDefault();
                        handleUserClick(mentionUserList[selectedIndex]);

                    }
                }

                if (event.key === "Escape") {
                    // ESC 키를 누르면 멘션박스 닫기
                    setMentionBoxVisible(false);
                    setMentionKeyword("");
                }
            };

            document.addEventListener("keydown", handleKeyDown);
            return () => document.removeEventListener("keydown", handleKeyDown);
        }
    }, [mentionUserList, mentionBoxVisible, selectedIndex]);

    // 멘션 유저 리스트 변경 시 선택중 데이터인 셀렉트 인덱스 초기화
    useEffect(() => {
        if (mentionBoxVisible) {
            setSelectedIndex(0)
        }

    }, [mentionUserList]);

    // @입력시 멘션 박스 생성하는 이벤트 등록
    useEffect(() => {
        if (quillRef.current) {
            const quillEditor = quillRef.current.getEditor();
            const editorContainer = quillEditor.root.parentNode; // ql-container를 감싸는 부모 요소
            quillEditor.focus();
            // `text-change` 이벤트 리스너
            // 1. 현재 커서 위치 가져옴
            // 2. 커서 위치 변경 시 커서에서 가장 가까운 @ 값 찾음
            // 3. @가 존재 할 시 @이후 텍스트 추출
            // 4. @이후 텍스트에 공백이나 줄바꿈이 포함 될 시 멘션박스 닫음
            // 5. 아닐 경우 맨션박스 열고 @이후 텍스트를 멘션키워드에 세팅
            quillEditor.on("text-change", (delta, oldDelta, source) => {
                if (source === "user") {
                    const text = quillEditor.getText(); // 에디터 전체 텍스트
                    const cursorPosition = quillEditor.getSelection()?.index; // 현재 커서 위치
                    if (cursorPosition) {

                        // 커서 이전 텍스트 가져오기
                        const textBeforeCursor = text.slice(0, cursorPosition);
                        // 커서 이전의 텍스트에서 가장 가까운 `@` 위치 찾기
                        const atIndex = textBeforeCursor.lastIndexOf("@");
                        if (atIndex !== -1) {
                            // `@` 이후 텍스트 추출
                            const mentionQuery = textBeforeCursor.slice(atIndex + 1);
                            // 공백 포함 시 멘션박스 닫고 mentionKeyword 초기화
                            if (mentionQuery.includes(" ")) {
                                setMentionBoxVisible(false);
                                setMentionKeyword(""); // mentionKeyword 초기화
                            } else {
                                setMentionBoxVisible(true);
                                setMentionKeyword(mentionQuery); // mentionKeyword 업데이트
                                // 멘션박스 위치 설정
                                setMentionTriggerBounds(quillEditor.getBounds(atIndex));
                                // 커서 위치 저장
                                setSavedCursorPosition(cursorPosition)
                            }
                        } else {
                            setMentionBoxVisible(false); // "@"가 없으면 멘션박스 숨김
                            setMentionKeyword("");
                            setSavedCursorPosition(null);
                        }
                    } else {
                        setMentionBoxVisible(false);
                        setMentionKeyword("");
                        setSavedCursorPosition(null);
                    }
                }
            });

            const handleEditorClick = () => {
                quillEditor.focus(); // 에디터에 강제로 포커스를 설정
            };

            // ql-container와 ql-snow 클래스를 클릭했을 때도 포커스를 설정하도록 이벤트 리스너 추가
            editorContainer.addEventListener('click', handleEditorClick);

            return () => {
                // 컴포넌트 언마운트 시 이벤트 리스너 제거
                editorContainer.removeEventListener('click', handleEditorClick);
            };
        }
    }, []);

    // 멘션 키워드 변경 시 그에 맞는 유저 리스트 호출
    useEffect(() => {
        if (mentionKeyword) {
            getUserList(mentionKeyword)
        } else if (userList) {
            // @만 입력해 멘션 키워드가 없는 상태일 경우 유저리스트의 사용자를 멘션유저리스트에 세팅
            const filterDepartmentUserList = userList.filter(user => user.isDepartment === "N");
            const uniqueUserList = Array.from(new Map(filterDepartmentUserList.map(user => [user.portalId, user])).values());

            setMentionUserList(uniqueUserList);
        } else {
            setMentionUserList([])
        }

    }, [mentionKeyword, userList]);

    // 사용자 목록 반환
    const getUserList = async (keyword) => {
        if (!keyword) {
            return ;
        }
        const params = { keyword, page: 1, limit:5, searchType:"userName" };
        const response = await axios.get(`/api/user/list${buildQuery(params)}`);
        setMentionUserList(response.data.itemList)
    }

    // 멘션 박스의 유저 클릭 시
    // 1. 저장해놓은 커서 위치와 멘션키워드 길이 반환
    // 2. 커서위치와 멘션키워드 길이를 이용해 @와 멘션키워드 제거
    // 3. 제거한 자리에 선택한 유저명으로 a링크 생성
    // 4. 생성 한 a링크에 data-userid 속성 추가 및 userId 값 삽입
    const handleUserClick = (user) => {
        if (quillRef.current && mentionUserList && mentionUserList.length > 0) {
            const quillEditor = quillRef.current.getEditor();
            const cursorPosition = savedCursorPosition || 0;
            const atIndex = cursorPosition - mentionKeyword.length - 1;
            quillEditor.deleteText(atIndex, mentionKeyword.length + 1);

            const mentionHtml = `<a href="userId${user.id ? user.id : user.userId}" onclick="return false;">${user.userFullName} ${user.userDepartment ? user.userDepartment : user.userDepartmentName}</a>`;
            quillEditor.clipboard.dangerouslyPasteHTML(atIndex, mentionHtml);

            const fileElement = quillEditor.root.querySelector(`[href="userId${user.id ? user.id : user.userId}"]`);
            if (fileElement) {
                fileElement.setAttribute('data-userid', user.id ? user.id : user.userId);
                fileElement.setAttribute('href', '#');

            }

            setMentionBoxVisible(false);
            setMentionKeyword("")
        }
    };

    return (
        <>
            <div
                style={{

                    width,
                    height,
                    maxHeight,
                    minHeight,
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    position: 'relative',
                }}
            >
                <style>
                    {`
                    /* 툴바 버튼 아이콘 기본 스타일 */
                    .ql-toolbar button:hover .ql-stroke,
                    .ql-toolbar button:focus .ql-stroke,
                    .ql-toolbar button.ql-active .ql-stroke {
                        stroke: #6A9C89 !important; /* 아이콘 색상을 변경 */
                    }
                
                    .ql-toolbar button:hover .ql-fill,
                    .ql-toolbar button:focus .ql-fill,
                    .ql-toolbar button.ql-active .ql-fill {
                        fill: #6A9C89 !important; /* 아이콘이 채워진 경우에도 색 적용 */
                    }

                    .ql-picker-label .ql-fill {
                        fill: inherit !important;
                    }
                    
                    /* 헤더의 화살표 아이콘 색상 변경 */
                    .ql-picker-label:hover .ql-stroke,
                    .ql-picker-label:focus .ql-stroke,
                    .ql-picker-label.ql-active .ql-stroke {
                        stroke: #6A9C89 !important; /* 헤더의 화살표 아이콘 색 변경 */
                    }
                    
                    /* 헤더의 화살표 아이콘이 채워진 경우 색상 변경 */
                    .ql-picker-label:hover .ql-fill,
                    .ql-picker-label:focus .ql-fill,
                    .ql-picker-label.ql-active .ql-fill {
                        fill: #6A9C89 !important;
                    }
                    
                    /* 드롭다운 메뉴에서 아이템 hover 시 색 변경 */
                    .ql-picker-options .ql-picker-item:hover {
                        color: #6A9C89 !important;
                    }
                
                    /* 드롭다운 메뉴에서 선택된 항목만 색 변경 */
                    .ql-picker-options .ql-picker-item.ql-selected {
                        color: #6A9C89 !important;
                    }
                
                    /* 툴바의 헤더(드롭다운 선택 값) 색 변경 */
                    .ql-picker-label.ql-active {
                        color: #6A9C89 !important; /* 활성화된 헤더 색상 변경 */
                    }
                
                    /* 툴바의 헤더에서 선택된 값 색 변경 */
                    .ql-picker.ql-header .ql-picker-label {
                        color: inherit !important; /* 기본값 유지 */
                    }
                
                    /* 툴바의 헤더에서 선택된 값이 있을 때 색 변경 */
                    .ql-picker.ql-header .ql-picker-label.ql-active {
                        color: #6A9C89 !important; /* 활성화된 헤더 색상 */
                    }
                    
                    /* 색상 변경 툴바의 드롭다운 항목 글자색 변경 */
                    .ql-toolbar .ql-color .ql-picker-options .ql-picker-item {
                        color: #000 !important; /* 검정색으로 변경 */
                    }
                    
                    /* 툴바 테두리 */
                    .ql-toolbar {
                        position: sticky; /* 툴바 고정 */
                        top: 0; /* 화면 최상단에 고정 */
                        z-index: 10;
                    }
                    
                    /* 에디터 영역 테두리 */
                    .ql-container {
                        height: ${editorBodyHeight};  /* 툴바 높이를 제외한 영역에 에디터 */
                        max-height: ${editorMaxHeight};  /* 최대로 커질 수 있는 높이 */
                        min-height: ${editorMinHeight};  /* 최대로 커질 수 있는 높이 */
                        overflow: auto; /* 스크롤은 에디터 내부에서만 생기도록 */
                    }
                    
                    /* 에디터 입력 영역 테두리 색을 진한 회색으로 변경 */
                    .ql-editor {
                        overflow: auto
                    }
                    
                    /* 아이콘들의 vertical alignment를 조정하여 밑으로 내려가는 문제 해결 */
                    .ql-toolbar button svg {
                        vertical-align: middle !important; /* 아이콘들이 중간에 위치하도록 조정 */
                    }
                `}
                </style>

                <ReactQuill
                    ref={quillRef}
                    value={editorValue}
                    onChange={handleTextChange}
                    modules={modules}
                    formats={formats}
                    theme="snow"
                    style={{
                        width: '100%',
                        height: '100%',
                        overflow: 'auto',
                    }}
                />
            </div>
            {mentionBoxVisible && (
                <div
                    ref={mentionBoxRef}
                    style={{
                        position: "absolute",
                        bottom: `${mentionBoxPosition.bottom}px`,
                        left: `${mentionBoxPosition.left}px`,
                        backgroundColor: "white",
                        border: "1px solid #ccc",
                        borderRadius: "4px",
                        boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
                        zIndex: 10,
                        padding: 0,
                        width: `${mentionBoxPosition.width}px`,
                        borderColor:'#043a5a'
                    }}
                >
                    {mentionUserList && mentionUserList.length > 0 ? mentionUserList.map((user, index) => (
                        <div
                            key={index}
                            style={{
                                padding: "8px 12px",
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                                backgroundColor: selectedIndex === index ? "#6A9C89" : "", // 선택된 데이터 강조
                                transition: "background-color 0.2s ease"
                            }}
                            onClick={() => handleUserClick(user)}
                            onMouseEnter={() => setSelectedIndex(index)}
                        >
                            <span style={{color: selectedIndex === index ? 'white' : 'black'}}>
                                {user.userFullName} {user.userDepartmentName ? user.userDepartmentName : user.userDepartment}
                            </span>
                            {user.userType &&
                                <span
                                    style={{
                                        fontSize: '11px',
                                        borderRadius: '10%',
                                        backgroundColor: '#385752',
                                        color: 'white',
                                        padding: '2px 5px',
                                        display: 'inline-block'
                                    }}
                                >
                                {user.userType === "BOM" ? "검토자"
                                    : user.userType === "PM" ? "요청자"
                                        : user.userType === "MEMBER" ? "참조자"
                                            : ""
                                }
                            </span>
                            }
                        </div>
                    )) : mentionKeyword ?
                        <div
                            style={{
                                padding: "8px 12px",
                                display: "flex",
                                alignItems: "center",
                                backgroundColor: "",
                                transition: "background-color 0.2s ease"
                            }}
                        >
                            <span style={{color: 'black'}}>
                                일치하는 사용자가 없습니다 : {mentionKeyword}
                            </span>
                        </div>
                        :
                        <div
                            style={{
                                padding: "8px 12px",
                                display: "flex",
                                alignItems: "center",
                            }}
                        >
                            <span style={{color: 'black'}}>
                                사용자를 입력해보세요.
                            </span>
                        </div>
                    }

                </div>
            )}
            {showFooter &&
                <div
                    style={{
                        backgroundColor: '',
                        height: '60px',
                        textAlign: 'right',
                        border: '1px solid #043a5a',
                        borderTop: 'none',
                        display: 'flex',
                        alignItems: 'flex-end',  // 요소들을 하단에 정렬
                        justifyContent: 'space-between',  // 좌우로 버튼과 글자 수 공간 배분
                        paddingBottom: '10px'  // 아래쪽에 여유 공간 추가
                    }}
                >
                    <span
                        style={{
                            float: 'left',
                            alignItems: 'center',
                            marginTop: '10px',
                            marginLeft: '5px',
                            fontWeight: 'bold',
                            color: textLength > maxChars && 'red'
                        }}
                    >
                        {textLength}/{maxChars}자
                    </span>
                    <div style={{display: 'flex', alignItems: 'center'}}>
                        {showCancelButton &&
                            <button
                                className="btn btn-secondary"
                                type="button"
                                onClick={handleCancel}
                                style={{marginRight: '10px', cursor: 'pointer'}}
                            >
                                취소
                            </button>
                        }
                        {showSaveButton &&
                            <button
                                className="btn btn-primary"
                                type="button"
                                onClick={handleSubmit}
                                style={{marginRight: '10px', cursor: 'pointer'}}
                            >
                                {saveButtonText}
                            </button>
                        }
                    </div>
                </div>
            }
        </>
    )
}

export default UpdateEditor;
