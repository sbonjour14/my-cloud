import {
    FieldGroup,
    FieldDescription,
    FieldLabel,
    FieldLegend,
    FieldSet,
    Field,
    FieldError,
} from "@/components/ui/field";

import { useForm } from "react-hook-form";
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "../ui/input-group";
import { Eye, EyeOff, Mail, UserIcon } from "lucide-react";
import { useState } from "react";
import type { RegisterFormBody } from "@/types";
import { Button } from "../ui/button";
import { toast } from "../ui/toast";
import { useNavigate } from "react-router-dom"
import { registerUser } from "@/lib/http-api/auth";
import axios from "axios";

const title = "Create your account";
const description = "Set up your personal cloud space in seconds.";
const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface RegisterFormValues extends RegisterFormBody {
    confirmPassword: string;
}


export function RegisterForm() {
    const {
        register,
        handleSubmit,
        watch,
        clearErrors,
        setError,
        formState: { errors },
    } = useForm<RegisterFormValues>({
        mode: "onSubmit",
        reValidateMode: "onSubmit",
    });

    const navigate = useNavigate();

    const [isPasswordShown, setIsPasswordShown] = useState<boolean>(false);
    const [isConfirmPasswordShown, setIsConfirmPasswordShown] = useState<boolean>(false);

    const passwordInputType = isPasswordShown ? "text" : "password";
    const confirmPasswordInputType = isConfirmPasswordShown ? "text" : "password";

    const togglePassword = () => setIsPasswordShown((p) => !p);
    const toggleRePassword = () => setIsConfirmPasswordShown((p) => !p);

    const password = watch("password");

    const onSubmit = async (data: RegisterFormValues) => {
        const {confirmPassword, ...payload} = data;
        try {
            await registerUser(payload);
            const id = toast.add({
                type: "success",
                description: "Account created with success",
                actionProps: {
                    children: "go to login",
                    onClick() {
                        navigate("/login");
                        toast.close(id);
                    }
                }
            })

        } catch (error) {
            if(axios.isAxiosError(error) && error.response) {
                const { message } = error.response.data;
                setError("form", { type: "server", message });
                return;
            }
            setError("form", { type: "server", message: "An unexpected error occurred" });
            return;
        }


    };

    return (
        <div className="w-full max-w-md p-4 bg-white rounded-lg shadow-md dark:bg-gray-800">
            <form onSubmit={handleSubmit(onSubmit)} onChange={() => clearErrors("form")}>
                <FieldGroup>
                    <FieldSet>
                        <FieldLegend>{title}</FieldLegend>
                        <FieldDescription>{description}</FieldDescription>
                        <p className="text-sm text-red-600 min-h-5">
                            {errors.form?.message}
                        </p>
                        <FieldGroup>
                            {/* EMAIL */}
                            <Field data-invalid={!!errors.email}>
                                <FieldLabel htmlFor="email"></FieldLabel>
                                <InputGroup>
                                    <InputGroupAddon align={"inline-start"}>
                                        <Mail />
                                    </InputGroupAddon>
                                    <InputGroupInput
                                        type="text"
                                        id="email"
                                        placeholder="Enter your email"
                                        {...register("email", {
                                            required: "Email is required",
                                            pattern: {
                                                value: emailRegex,
                                                message: "Invalid email format",
                                            },
                                        })}
                                        aria-invalid={!!errors.email}
                                        onChange={() => clearErrors("email")}
                                    />
                                </InputGroup>
                                {!!errors.email && <FieldError>{errors.email?.message as string}</FieldError>}
                            </Field>

                            {/* USERNAME */}
                            <Field data-invalid={!!errors.displayName}>
                                <FieldLabel htmlFor="username"></FieldLabel>
                                <InputGroup>
                                    <InputGroupAddon align={"inline-start"}>
                                        <UserIcon />
                                    </InputGroupAddon>
                                    <InputGroupInput
                                        type="text"
                                        id="username"
                                        placeholder="Enter your username"
                                        {...register("displayName", {
                                            required: "Username is required",
                                            minLength: {
                                                value: 3,
                                                message: "Username must be at least 3 characters",
                                            },
                                        })}
                                        aria-invalid={!!errors.displayName}
                                    />
                                </InputGroup>
                                {!!errors.displayName && <FieldError>{errors.displayName?.message as string}</FieldError>}
                            </Field>

                            {/* PASSWORD */}
                            <Field data-invalid={!!errors.password}>
                                <FieldLabel htmlFor="password"></FieldLabel>
                                <InputGroup>
                                    <InputGroupInput
                                        type={passwordInputType}
                                        id="password"
                                        placeholder="Enter your password"
                                        {...register("password", {
                                            required: "Password is required",
                                            minLength: {
                                                value: 8,
                                                message: "Password must be at least 8 characters",
                                            },
                                        })}
                                        aria-invalid={!!errors.password}
                                    />
                                    <InputGroupAddon align={"inline-end"}>
                                        <InputGroupButton
                                            tabIndex={-1}
                                            aria-label={isPasswordShown ? "hide password" : "show password"}
                                            onClick={togglePassword}
                                        >
                                            {isPasswordShown ? <Eye /> : <EyeOff />}
                                        </InputGroupButton>
                                    </InputGroupAddon>
                                </InputGroup>
                                {!!errors.password && <FieldError>{errors.password?.message as string}</FieldError>}
                            </Field>

                            {/* CONFIRM PASSWORD */}
                            <Field data-invalid={!!errors.confirmPassword}>
                                <FieldLabel htmlFor="re-password"></FieldLabel>
                                <InputGroup>
                                    <InputGroupInput
                                        type={confirmPasswordInputType}
                                        id="re-password"
                                        placeholder="Confirm your password"
                                        {...register("confirmPassword", {
                                            required: "Please confirm your password",
                                            validate: (value) =>
                                                value === password || "Passwords do not match",
                                        })}
                                        aria-invalid={!!errors.confirmPassword}
                                    />
                                    <InputGroupAddon align={"inline-end"}>
                                        <InputGroupButton
                                            tabIndex={-1}
                                            aria-label={isConfirmPasswordShown ? "hide password" : "show password"}
                                            onClick={toggleRePassword}
                                        >
                                            {isConfirmPasswordShown ? <Eye /> : <EyeOff />}
                                        </InputGroupButton>
                                    </InputGroupAddon>
                                </InputGroup>
                                {!!errors.confirmPassword && (
                                    <FieldError>{errors.confirmPassword?.message as string}</FieldError>
                                )}
                            </Field>
                        </FieldGroup>
                    </FieldSet>
                    <Field orientation="responsive">
                        <Button type="submit">Submit</Button>
                    </Field>
                </FieldGroup>
            </form>
        </div>
    );
}